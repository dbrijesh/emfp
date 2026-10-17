// __tests__/EmployeeForm.test.js
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import EmployeeForm from '../src/components/EmployeeForm';
import * as empService from '../src/services/employeeService';
import * as deptService from '../src/services/departmentService';

jest.mock('react-router-dom', () => {
  const actual = jest.requireActual('react-router-dom');
  return { ...actual, useNavigate: () => jest.fn() };
});

jest.spyOn(empService, 'addEmployee');
jest.spyOn(empService, 'getEmployeeById');
jest.spyOn(empService, 'updateEmployee');
jest.spyOn(deptService, 'getAllDepartments');

const renderAt = path =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/add-employee" element={<EmployeeForm />} />
        <Route path="/edit-employee/:id" element={<EmployeeForm />} />
      </Routes>
    </MemoryRouter>
  );

describe('<EmployeeForm /> status control', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    deptService.getAllDepartments.mockResolvedValue([{ id: 1, name: 'IT' }]);
  });

  it('defaults to active on /add-employee and submits active (AC13)', async () => {
    empService.addEmployee.mockResolvedValue({});
    renderAt('/add-employee');
    const toggle = await screen.findByRole('checkbox', { name: /active/i });
    expect(toggle).toBeChecked();
    fireEvent.change(screen.getByLabelText(/first name/i), { target: { name: 'firstName', value: 'A' } });
    fireEvent.change(screen.getByLabelText(/last name/i), { target: { name: 'lastName', value: 'B' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'a@b.co' } });
    fireEvent.change(screen.getByLabelText(/age/i), { target: { name: 'age', value: '30' } });
    fireEvent.submit(toggle.closest('form'));
    await waitFor(() => expect(empService.addEmployee).toHaveBeenCalled());
    expect(empService.addEmployee.mock.calls[0][0]).toMatchObject({ active: true });
  });

  it('shows the stored inactive value when editing (AC13)', async () => {
    empService.getEmployeeById.mockResolvedValue({ id: 7, firstName: 'A', lastName: 'B', email: 'a@b.co', age: 30, active: false, department: { id: 1, name: 'IT' } });
    renderAt('/edit-employee/7');
    const toggle = await screen.findByRole('checkbox', { name: /active/i });
    expect(toggle).not.toBeChecked();
  });
});
