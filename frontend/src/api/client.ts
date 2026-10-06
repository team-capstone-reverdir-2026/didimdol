import axios from 'axios';

const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    withCredentials: true,
    headers: {'Content-Type': 'application/json'},
});

export default client;