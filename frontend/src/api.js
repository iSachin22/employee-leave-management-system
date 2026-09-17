import axios from 'axios';

const API = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

export const fetchLeaveRequests = async (employeeId) => {
  try {
    const response = await API.get(`/leave/employee/${employeeId}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const applyLeave = async (employeeId, payload) => {
  const response = await API.post(`/leave/apply/${employeeId}`, payload);
  return response.data;
};
