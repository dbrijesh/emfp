package com.example.employeemanagement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.employeemanagement.model.Department;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.model.User;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full-context integration tests for the employee status toggle ({@code PATCH
 * /api/employees/{id}/status}) and the {@code active} field on the employee create/update/read
 * endpoints. Runs against an in-memory H2 database.
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:statustest;DB_CLOSE_DELAY=-1;MODE=MySQL",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "jwt.secret=integration-test-secret-key-please-do-not-use-in-production-0001",
      "webauthn.rp-id=localhost",
      "webauthn.rp-name=Employee Management System",
      "webauthn.allowed-origins=http://localhost:3000",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration"
    })
@AutoConfigureMockMvc
class EmployeeStatusApiTest {

  /** MockMvc for issuing requests against the running context. */
  @Autowired private MockMvc mockMvc;

  /** Repository used to seed employees. */
  @Autowired private EmployeeRepository employeeRepository;

  /** Repository used to seed departments. */
  @Autowired private DepartmentRepository departmentRepository;

  /** Repository used to seed the authenticated user. */
  @Autowired private UserRepository userRepository;

  /** Used to mint a valid JWT. */
  @Autowired private JwtTokenUtil jwtTokenUtil;

  /** Department shared by the seeded employees. */
  private Department department;

  /** Bearer header value for the seeded user. */
  private String bearer;

  /** Seeds a user, a department and resets employees before each test. */
  @BeforeEach
  void setUp() {
    employeeRepository.deleteAll();
    if (!userRepository.findByUsername("status-tester").isPresent()) {
      User user = new User();
      user.setUsername("status-tester");
      user.setPassword("irrelevant");
      userRepository.save(user);
    }
    bearer = "Bearer " + jwtTokenUtil.generateToken("status-tester");
    department = new Department();
    department.setName("Dept-" + System.nanoTime());
    department = departmentRepository.save(department);
  }

  /** Creates and persists an employee with the given status. */
  private Employee seed(boolean active) {
    Employee employee = new Employee();
    employee.setFirstName("Ada");
    employee.setLastName("Lovelace");
    employee.setEmail("ada" + System.nanoTime() + "@example.com");
    employee.setAge(30);
    employee.setDepartment(department);
    employee.setActive(active);
    return employeeRepository.save(employee);
  }

  /** Builds a create/update request body. */
  private String body(String activeJson) {
    return "{\"firstName\":\"Grace\",\"lastName\":\"Hopper\",\"email\":\"grace"
        + System.nanoTime()
        + "@example.com\",\"age\":40,\"department\":{\"id\":"
        + department.getId()
        + "}"
        + activeJson
        + "}";
  }

  /** AC1: an entity that never had its status set is persisted as active. */
  @Test
  void newEntityDefaultsToActive() {
    Employee employee = new Employee();
    employee.setFirstName("Def");
    employee.setLastName("Ault");
    employee.setEmail("default@example.com");
    employee.setAge(30);
    employee.setDepartment(department);
    Employee saved = employeeRepository.save(employee);
    org.junit.jupiter.api.Assertions.assertTrue(
        employeeRepository.findById(saved.getId()).get().isActive());
  }

  /** AC2: POST without active yields active=true. */
  @Test
  void createWithoutActiveDefaultsToTrue() throws Exception {
    mockMvc
        .perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body("")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.active").value(true));
  }

  /** AC8: POST with active=false is honoured. */
  @Test
  void createWithActiveFalseIsInactive() throws Exception {
    mockMvc
        .perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(",\"active\":false")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.active").value(false));
  }

  /** AC8: PUT without active leaves an inactive employee inactive. */
  @Test
  void putWithoutActiveKeepsStatus() throws Exception {
    Employee employee = seed(false);
    mockMvc
        .perform(
            put("/api/employees/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
  }

  /** AC8: PUT with active=true activates an inactive employee. */
  @Test
  void putWithActiveTrueActivates() throws Exception {
    Employee employee = seed(false);
    mockMvc
        .perform(
            put("/api/employees/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(",\"active\":true")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(true));
  }

  /** AC3: list and by-id responses carry a boolean active. */
  @Test
  void getResponsesIncludeActive() throws Exception {
    Employee employee = seed(false);
    mockMvc
        .perform(get("/api/employees"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].active").value(false));
    mockMvc
        .perform(get("/api/employees/" + employee.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
  }

  /** AC4: PATCH deactivates and the change persists. */
  @Test
  void patchSetsStatusAndPersists() throws Exception {
    Employee employee = seed(true);
    mockMvc
        .perform(
            patch("/api/employees/" + employee.getId() + "/status")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
    mockMvc
        .perform(get("/api/employees/" + employee.getId()))
        .andExpect(jsonPath("$.active").value(false));
  }

  /** AC5: PATCH changes nothing but the status. */
  @Test
  void patchChangesOnlyStatus() throws Exception {
    Employee employee = seed(true);
    mockMvc
        .perform(
            patch("/api/employees/" + employee.getId() + "/status")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value(employee.getFirstName()))
        .andExpect(jsonPath("$.lastName").value(employee.getLastName()))
        .andExpect(jsonPath("$.email").value(employee.getEmail()))
        .andExpect(jsonPath("$.age").value(employee.getAge()))
        .andExpect(jsonPath("$.department.id").value(department.getId()));
  }

  /** AC6: unknown id yields 404. */
  @Test
  void patchUnknownIdIsNotFound() throws Exception {
    mockMvc
        .perform(
            patch("/api/employees/999999/status")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
        .andExpect(status().isNotFound());
  }

  /** AC6: missing active yields 400 and leaves the status unchanged. */
  @Test
  void patchMissingActiveIsBadRequest() throws Exception {
    Employee employee = seed(true);
    mockMvc
        .perform(
            patch("/api/employees/" + employee.getId() + "/status")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
    org.junit.jupiter.api.Assertions.assertTrue(
        employeeRepository.findById(employee.getId()).get().isActive());
  }

  /** AC6: non-boolean active yields 400 and leaves the status unchanged. */
  @Test
  void patchNonBooleanActiveIsBadRequest() throws Exception {
    Employee employee = seed(true);
    mockMvc
        .perform(
            patch("/api/employees/" + employee.getId() + "/status")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":\"yes\"}"))
        .andExpect(status().isBadRequest());
    org.junit.jupiter.api.Assertions.assertTrue(
        employeeRepository.findById(employee.getId()).get().isActive());
  }

  /** AC7: no token yields 401 and leaves the status unchanged. */
  @Test
  void patchWithoutTokenIsUnauthorized() throws Exception {
    Employee employee = seed(true);
    mockMvc
        .perform(
            patch("/api/employees/" + employee.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
        .andExpect(status().isUnauthorized());
    org.junit.jupiter.api.Assertions.assertTrue(
        employeeRepository.findById(employee.getId()).get().isActive());
  }
}
