package ua.artcode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ua.artcode.enums.UserType.*;

/**
 * Created by zhenia on 27.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;


    @Test
    public void testRegistrationTeacher() throws Exception {
        mockMvc.perform(post("/register?login=Username1&email=42004200zhenia@gmail.com&pass=password1&type=TEACHER")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    public void testRegistrationStudent() throws Exception {
        mockMvc.perform(post("/register?login=Username51&email=42004200zhenia1@gmail.com&pass=password1&type=STUDENT")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testRegistrationNegative() throws Exception {
        mockMvc.perform(post("/register?login=Us&email=42004200zhenia2@gmail.com&pass=password1&type=STUDENT")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));


        mockMvc.perform(post("/register?login=Username2&email=42004200zheniagmail.com&pass=password1&type=STUDENT")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));

        mockMvc.perform(post("/register?login=Username3&email=42004200zhenia@gmail.com&pass=p1&type=STUDENT")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void testLoginPositive() throws Exception {
        mockMvc.perform(post("/register?login=Username71&email=42004200zhenia71@gmail.com&pass=password1&type=STUDENT")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(mockMvc.perform(post("/login?login=Username71&pass=password1"))
                .andReturn()
                .getResponse()
                .getContentAsString(), notNullValue());

        assertThat(mockMvc.perform(post("/login?login=42004200zhenia71@gmail.com&pass=password1"))
                .andReturn()
                .getResponse()
                .getContentAsString(), notNullValue());
    }

    @Test
    public void testLoginNegative() throws Exception {
        mockMvc.perform(post("/register?login=Username72&email=42004200zhenia72@gmail.com&pass=password1&type=STUDENT")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(mockMvc.perform(post("/login?login=Username72&pass=password2"))
                .andReturn()
                .getResponse()
                .getContentAsString(), isEmptyOrNullString());

        assertThat(mockMvc.perform(post("/login?login=Username72&pass=pas"))
                .andReturn()
                .getResponse()
                .getContentAsString(), isEmptyOrNullString());
    }

}
