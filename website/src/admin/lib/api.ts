import axios from "axios";
import { token as sharedToken } from "../../lib/api";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
});

api.interceptors.request.use((config) => {
  const token = sharedToken.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
