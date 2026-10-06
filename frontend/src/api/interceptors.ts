import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import { ApiEndpoints } from './endpoints';
import { type TokenRefreshResponse } from './auth';

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

export function setupInterceptors(axiosInstance: AxiosInstance, onTokenInvalidated?: () => void) {
  
  axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      if (ApiEndpoints.isPublicPath(config.url)) {
        return config;
      }

      const accessToken = localStorage.getItem('accessToken');
      if (accessToken && config.headers) {
        config.headers.Authorization = `Bearer ${accessToken}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  axiosInstance.interceptors.response.use(
    (response) => response,
    
    async (error) => {
      const originalRequest = error.config;
      const isUnauthorized = error.response?.status === 401;
      const path = originalRequest?.url;

      if (!isUnauthorized || ApiEndpoints.isPublicPath(path)) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axiosInstance(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      //401에러 발생 지점인 경우
      isRefreshing = true;
      originalRequest._retry = true;

      try {
        const currentRefreshToken = localStorage.getItem('refreshToken');
        if (!currentRefreshToken) {
          throw new Error('Refresh token missing');
        }

        const refreshResponse = await axios.post<TokenRefreshResponse>(
          ApiEndpoints.refresh,
          { refreshToken: currentRefreshToken }
        );

        if (refreshResponse.data.success) {
          const { accessToken: newAccess, refreshToken: newRefresh } = refreshResponse.data.data;

          localStorage.setItem('accessToken', newAccess);
          if (newRefresh) {
            localStorage.setItem('refreshToken', newRefresh);
          }

          processQueue(null, newAccess);

          originalRequest.headers.Authorization = `Bearer ${newAccess}`;
          return axiosInstance(originalRequest);
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        
        if (onTokenInvalidated) {
          onTokenInvalidated();
        }
        
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
  );
}