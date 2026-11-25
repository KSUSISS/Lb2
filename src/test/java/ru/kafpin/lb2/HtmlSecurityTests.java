package ru.kafpin.lb2;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import ru.kafpin.lb2.entity.Role;
import ru.kafpin.lb2.repository.SchoolRepository;
import ru.kafpin.lb2.repository.SchoolchildRepository;
import ru.kafpin.lb2.repository.UserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:html-security;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HtmlSecurityTests {
    private static final String TEST_PASSWORD = "test-only-password";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired SchoolRepository schools;
    @Autowired SchoolchildRepository schoolchildren;

    @BeforeAll
    void createTestUsers() {
        users.insert("reader", encoder.encode(TEST_PASSWORD), Role.ROLE_USER);
        users.insert("registrar", encoder.encode(TEST_PASSWORD), Role.ROLE_ADMIN);
    }

    @Test
    void guestGetsCustomLoginAndIncorrectPasswordShowsError() throws Exception {
        mvc.perform(get("/schoolchildren"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Вход в реестр")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/login").with(csrf()).param("username", "reader").param("password", "incorrect"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
        mvc.perform(get("/login").param("error", ""))
                .andExpect(content().string(containsString("Неверный логин или пароль.")));
    }

    @Test
    void userCanReadAndSearchButCannotOpenOrSubmitAddForms() throws Exception {
        var session = login("reader", "USER");
        mvc.perform(get("/schools").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("reader")))
                .andExpect(content().string(not(containsString("href=\"/schools/new\""))));
        mvc.perform(get("/schoolchildren/search/lastname").session(session).param("lastName", "Тестов"))
                .andExpect(status().isOk());
        for (String path : new String[]{"/schools/new", "/schoolchildren/new"}) {
            mvc.perform(get(path).session(session))
                    .andExpect(status().isForbidden())
                    .andExpect(forwardedUrl("/forbidden"));
            mvc.perform(post(path).session(session).with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void adminCreatesSchoolAndPupilThroughFormsAndCanLogOut() throws Exception {
        var session = login("registrar", "ADMIN");
        int schoolCount = schools.findAll().size();
        mvc.perform(get("/schools/new").session(session)).andExpect(status().isOk());
        mvc.perform(post("/schools/new").session(session).param("name", "Школа без CSRF"))
                .andExpect(status().isForbidden());
        assertEquals(schoolCount, schools.findAll().size());
        mvc.perform(post("/schools/new").session(session).with(csrf()).param("name", "Школа ЛБ6"))
                .andExpect(redirectedUrl("/schools"));
        var school = schools.findAll().stream()
                .filter(candidate -> candidate.getName().equals("Школа ЛБ6")).findFirst().orElseThrow();
        mvc.perform(get("/schoolchildren/new").session(session)).andExpect(status().isOk());
        mvc.perform(post("/schoolchildren/new").session(session).with(csrf()).param("schoolId", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Выберите школу из списка.")));
        mvc.perform(post("/schoolchildren/new").session(session).with(csrf())
                        .param("schoolId", Long.toString(school.getId()))
                        .param("lastName", "Тестов").param("firstName", "Иван").param("birthDate", "2012-05-15")
                        .param("schoolClass", "7А").param("height", "160").param("weight", "50")
                        .param("address.city", "Казань").param("address.street", "Школьная").param("address.house", "1"))
                .andExpect(redirectedUrl("/schoolchildren"));
        var pupil = schoolchildren.findByLastNameContainsIgnoreCase("Тестов").get(0);
        assertEquals(school.getId(), pupil.getSchool().getId());
        assertEquals("Казань", pupil.getAddress().getCity());
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout"));
        assertTrue(session.isInvalid());
    }

    private MockHttpSession login(String username, String role) throws Exception {
        var result = mvc.perform(post("/login").with(csrf()).param("username", username).param("password", TEST_PASSWORD))
                .andExpect(authenticated().withRoles(role))
                .andExpect(redirectedUrl("/schools"))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
