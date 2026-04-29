package com.cpvt.prereq_visualizer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cpvt.prereq_visualizer.model.UserCreateRequestModel;
import com.cpvt.prereq_visualizer.model.UserDetailModel;
import com.cpvt.prereq_visualizer.model.UserModel;
import com.cpvt.prereq_visualizer.model.UserPatchRequestModel;
import com.cpvt.prereq_visualizer.service.UserConflictException;
import com.cpvt.prereq_visualizer.service.UserService;
import com.cpvt.prereq_visualizer.service.UserValidationException;

@RestController
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping({ "/api/users", "/api/users/" })
	public List<UserModel> getAllUsers() {
		return userService.getAllUsers();
	}

	@GetMapping({ "/api/users/{id}", "/api/users/{id}/" })
	public UserDetailModel getUserById(@PathVariable("id") Integer id) {
		return userService.getUserById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	@PostMapping({ "/api/users", "/api/users/" })
	@ResponseStatus(HttpStatus.CREATED)
	public UserDetailModel createUser(@RequestBody UserCreateRequestModel request) {
		try {
			return userService.createUser(request);
		} catch (UserValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (UserConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}

	@PatchMapping({ "/api/users/{id}", "/api/users/{id}/" })
	public UserDetailModel updateUser(
			@PathVariable("id") Integer id,
			@RequestBody UserPatchRequestModel request) {
		try {
			return userService.updateUser(id, request)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		} catch (UserValidationException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		} catch (UserConflictException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
		}
	}

	@DeleteMapping({ "/api/users/{id}", "/api/users/{id}/" })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable("id") Integer id) {
		if (!userService.deleteUser(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
	}
}