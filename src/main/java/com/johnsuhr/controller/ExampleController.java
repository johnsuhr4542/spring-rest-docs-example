package com.johnsuhr.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/example")
public class ExampleController {

	private List<Person> people;

	public ExampleController() {
		people = new ArrayList<>();
		people.add(new Person("john", 25));
		people.add(new Person("david", 19));
	}

	@GetMapping
	public ResponseEntity<?> findAll() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", "ok");
		body.put("payload", people);

		return ok(body);
	}

	@GetMapping("/{name}")
	public ResponseEntity<?> findOne(@PathVariable String name) {
		Map<String, Object> body = new LinkedHashMap<>();
		Person person = people.stream()
			.filter(p -> name.equals(p.getName()))
			.findFirst()
			.orElse(null);
		boolean exists = person != null;
		body.put("status", exists ? "ok" : "fail");
		body.put("payload", person);

		return ok(body);
	}

	@PostMapping
	public ResponseEntity<?> save(@RequestBody Person person) {
		people.add(person);
		Map<String, Object> body = Map.of("status", "ok");

		return ok(body);
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Person {
		private String name;
		private Integer age;
	}

}
