package ru.kafpin.lb2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.kafpin.lb2.entity.School;
import ru.kafpin.lb2.repository.AddressRepository;
import ru.kafpin.lb2.repository.SchoolRepository;
import ru.kafpin.lb2.repository.SchoolchildRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:resttest;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always"
})
class RestApiIntegrationTests {
    @LocalServerPort int port;
    @Autowired SchoolRepository schools;
    @Autowired SchoolchildRepository children;
    @Autowired AddressRepository addresses;
    final HttpClient client = HttpClient.newHttpClient();
    long schoolId;

    @BeforeEach
    void prepare() {
        children.deleteAll();
        schools.deleteAll();
        School school = new School();
        school.setName("Test school");
        schoolId = schools.save(school).getId();
    }

    HttpResponse<String> request(String method, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    String payload(long school) {
        return """
                {"lastName":"Test","firstName":"Ivan","middleName":"Ivanovich",
                 "gender":"M","nationality":"Test","height":160,"weight":50,
                 "birthDate":"2012-05-15","phone":"+70000000000","schoolClass":"7A",
                 "schoolId":%d,"address":{"postalCode":"000000","country":"Test",
                 "region":"Test","district":"Test","city":"Test City","street":"Test",
                 "house":"1","apartment":"2"}}
                """.formatted(school);
    }

    long createChild() throws Exception {
        var response = request("POST", "/api/schoolchildren", payload(schoolId));
        assertEquals(201, response.statusCode(), response.body());
        var matcher = Pattern.compile("\"id\":(\\d+)").matcher(response.body());
        assertTrue(matcher.find());
        return Long.parseLong(matcher.group(1));
    }

    @Test
    void fullCrudPreservesAddressIdAndRemovesAddressOnDelete() throws Exception {
        long id = createChild();
        long addressId = children.findById(id).orElseThrow().getAddress().getId();
        var read = request("GET", "/api/schoolchildren/" + id, null);
        assertEquals(200, read.statusCode());
        assertTrue(read.body().contains("\"phone\":\"+70000000000\""));
        assertTrue(read.body().contains("\"city\":\"Test City\""));
        assertTrue(read.body().contains("\"birthDate\":\"2012-05-15\""));
        var update = request("PUT", "/api/schoolchildren/" + id,
                payload(schoolId).replace("Test City", "New City").replace("7A", "8A"));
        assertEquals(200, update.statusCode(), update.body());
        assertEquals(addressId, children.findById(id).orElseThrow().getAddress().getId());
        assertEquals("New City", addresses.findById(addressId).orElseThrow().getCity());
        assertEquals(204, request("DELETE", "/api/schoolchildren/" + id, null).statusCode());
        assertFalse(addresses.existsById(addressId));
        assertTrue(schools.existsById(schoolId));
        assertEquals(404, request("DELETE", "/api/schoolchildren/" + id, null).statusCode());
    }

    @Test
    void rejectsInvalidNestedDataAndUnknownSchoolWithoutWritingRows() throws Exception {
        var invalid = request("POST", "/api/schoolchildren",
                payload(schoolId).replace("\"city\":\"Test City\"", "\"city\":\"\""));
        assertEquals(400, invalid.statusCode());
        assertTrue(invalid.body().contains("address.city"));
        assertEquals(404, request("POST", "/api/schoolchildren", payload(999999)).statusCode());
        assertEquals(0, children.count());
        assertEquals(0, addresses.count());
    }

    @Test
    void patchUpdatesOnlyGivenFieldsAndRejectsEntireInvalidRequest() throws Exception {
        long id = createChild();
        String path = "/api/schoolchildren/" + id;
        assertEquals(200, request("PATCH", path, "{\"schoolClass\":\"8B\"}").statusCode());
        var saved = children.findById(id).orElseThrow();
        assertEquals("8B", saved.getSchoolClass());
        assertEquals("+70000000000", saved.getPhone());
        assertEquals(400, request("PATCH", path,
                "{\"phone\":\"changed\",\"schoolClass\":\" \"}").statusCode());
        assertEquals("+70000000000", children.findById(id).orElseThrow().getPhone());
        assertEquals(400, request("PATCH", path, "{}").statusCode());
    }

    @Test
    void schoolCrudAndDeletionGuardProtectChildren() throws Exception {
        var create = request("POST", "/api/schools", "{\"name\":\"Temporary\"}");
        assertEquals(201, create.statusCode());
        var matcher = Pattern.compile("\"id\":(\\d+)").matcher(create.body());
        assertTrue(matcher.find());
        String path = "/api/schools/" + matcher.group(1);
        assertEquals(200, request("PUT", path, "{\"name\":\"Renamed\"}").statusCode());
        assertEquals(204, request("DELETE", path, null).statusCode());
        assertEquals(404, request("GET", path, null).statusCode());
        assertEquals(400, request("POST", "/api/schools", "{\"name\":\" \"}").statusCode());
        long childId = createChild();
        assertEquals(409, request("DELETE", "/api/schools/" + schoolId, null).statusCode());
        assertTrue(children.existsById(childId));
        assertTrue(schools.existsById(schoolId));
    }

    @Test
    void statisticsAndMissingIdsHaveExpectedResults() throws Exception {
        var empty = request("GET", "/api/schoolchildren/statistics", null);
        assertEquals(200, empty.statusCode());
        assertTrue(empty.body().contains("\"total\":0"));
        createChild();
        var populated = request("GET", "/api/schoolchildren/statistics", null);
        assertTrue(populated.body().contains("\"total\":1"));
        assertTrue(populated.body().contains("\"" + schoolId + "\":1"));
        assertTrue(populated.body().contains("\"7A\":1"));
        assertEquals(404, request("GET", "/api/schoolchildren/999999", null).statusCode());
        assertEquals(404, request("PUT", "/api/schoolchildren/999999", payload(schoolId)).statusCode());
        assertEquals(404, request("PATCH", "/api/schoolchildren/999999", "{\"schoolClass\":\"9A\"}").statusCode());
        assertEquals(400, request("GET", "/api/schoolchildren/not-a-number", null).statusCode());
    }
}
