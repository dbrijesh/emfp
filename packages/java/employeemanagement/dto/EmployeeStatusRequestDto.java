package com.example.employeemanagement.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for employee status update requests.
 *
 * <p>Carries only the new active/inactive status of an employee.
 */
@Data
@NoArgsConstructor
public class EmployeeStatusRequestDto {

  /** The new status of the employee: {@code true} for active, {@code false} for inactive. */
  @NotNull(message = "Active status is required")
  private Boolean active;
}
