package com.johnsuhr.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnsuhr.controller.ExampleController;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExampleControllerTest {

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();
	@Autowired
	private WebApplicationContext wac;
	private MockMvc mvc;
	private RestDocumentationResultHandler document;

	@Before
	public void setup() {
		document = document(
			"{class-name}/{method-name}",
			preprocessResponse(prettyPrint())
		);
		mvc = webAppContextSetup(wac)
			.apply(documentationConfiguration(restDocumentation)
				.uris().withScheme("https").withHost("www.api").withPort(443))
			.alwaysDo(document)
			.build();
	}

	@Test
	public void registerPersonTest() throws Exception {
		ExampleController.Person person = new ExampleController.Person();
		person.setName("charlie");
		person.setAge(21);

		mvc.perform(post("/example")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(person)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document.document(
				requestFields(
					fieldWithPath("name").description("이름"),
					fieldWithPath("age").description("나이")
				),
				responseFields(
					fieldWithPath("status").description("응답 결과")
				)
			))
			.andExpect(jsonPath("status").value("ok"));
	}

	@Test
	public void findPersonTest() throws Exception {
		mvc.perform(get("/example/{name}", "john")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document.document(
				pathParameters(
					parameterWithName("name").description("이름")
				),
				responseFields(
					fieldWithPath("status").description("응답 결과"),
					fieldWithPath("payload.name").description("이름"),
					fieldWithPath("payload.age").type(Integer.class).description("나이")
				)
			))
			.andExpect(jsonPath("status").value("ok"))
			.andExpect(jsonPath("payload.name").value("john"))
			.andExpect(jsonPath("payload.age").value(25));
	}

	@Test
	public void findAllPeopleTest() throws Exception {
		mvc.perform(get("/example").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document.document(
				responseFields(
					fieldWithPath("status").description("응답 결과"),
					fieldWithPath("payload[].name").description("이름"),
					fieldWithPath("payload[].age").type(Integer.class).description("나이")
				)
			))
			.andExpect(jsonPath("status").value("ok"))
			.andExpect(jsonPath("payload[0].name").exists())
			.andExpect(jsonPath("payload[0].age").exists());
	}

}
