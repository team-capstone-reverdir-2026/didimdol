import axios from 'axios';
import client from './client';
import { ApiEndpoints } from './endpoints';

export interface SignInRequest {
    username: string;
    password: string;
}

export interface SignInResponse {
    success: boolean;
    message: string;
    data: {
        memberId: number;
        accessToken: string;
        refreshToken: string;
        expiresIn: number;
        expiresAt: string;
    };
}

export const signIn = async (request: SignInRequest): Promise<SignInResponse> => {
    const response = await client.post('/api/auth/sign-in', request);
    return response.data;
};

export interface SignOutRequest {
    accessToken: string;
}

export interface SignOutResponse {
    success: boolean;
    message: string;
    data: null;
}

export const signOut = async (request: SignOutRequest): Promise<SignOutResponse> => {
    const response = await client.post('/api/auth/sign-out', request);
    return response.data;
};

export interface SignUpRequest {
    username: string;
    password: string;
    nickname: string;
}

export interface SignUpResponse {
    success: boolean;
    message: string;
    data: null;
}

export const signUp = async (request: SignUpRequest): Promise<SignUpResponse> => {
    const response = await client.post('/api/auth/sign-up', request);
    return response.data;
};

export interface TokenRefreshRequest {
    refreshToken: string;
}

export interface TokenRefreshResponse {
    success: boolean;
    message: string;
    data: {
        accessToken: string;
        refreshToken: string;
        expiresIn: number;
        expiresAt: string;
    };
}

let refreshTimeoutId: number | null = null;

export const scheduleSilentRefresh = (expiresIn: number) => {
  if (refreshTimeoutId) {
    window.clearTimeout(refreshTimeoutId);
  }

  const refreshDelayInMs = (expiresIn - 300) * 1000; 
  const safeDelay = refreshDelayInMs > 0 ? refreshDelayInMs : 1000;

  console.log(`${safeDelay / 1000}초 뒤에 자동으로 토큰 재발급이 실행됩니다.`);

  refreshTimeoutId = window.setTimeout(async () => {
    try {
      const currentRefreshToken = localStorage.getItem('refreshToken');
      if (!currentRefreshToken) return;

      console.log('자동으로 토큰 재발급을 요청합니다.');
      
      const res = await axios.post<TokenRefreshResponse>(ApiEndpoints.refresh, {
        refreshToken: currentRefreshToken,
      });

      if (res.data.success) {
        const { accessToken, refreshToken, expiresIn: newExpiresIn } = res.data.data;
        
        localStorage.setItem('accessToken', accessToken);
        if (refreshToken) {
          localStorage.setItem('refreshToken', refreshToken);
        }

        console.log('Refresh 성공. 새로운 타이머를 세팅합니다.');
        scheduleSilentRefresh(newExpiresIn);
      }
    } catch (error) {
      console.error('Silent Refresh 자동 재발급 실패:', error);
      localStorage.clear();
      window.location.href = '/';
    }
  }, safeDelay);
};

export const clearSilentRefresh = () => {
  if (refreshTimeoutId) {
    window.clearTimeout(refreshTimeoutId);
    refreshTimeoutId = null;
    console.log('정상 로그아웃으로 자동 재발급 타이머가 종료되었습니다.');
  }
};