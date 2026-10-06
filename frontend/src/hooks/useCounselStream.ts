import { useEffect, useState } from 'react';
import { type ClientInfo } from '../api/counsel';

export interface SseMessageData {
  messageId: number;
  emotion: 'NEUTRAL' | 'HAPPY' | 'SAD' | 'ANGRY';
  content: string;
}

export interface SseErrorData {
  messageId: number;
  message: string;
}

export interface SseCloseData {
  reason: 'COUNSEL_COMPLETED' | string;
}

interface UseCounselStreamProps {
  sessionId: number;
  sseTicket: string;
}

//실시간 스트림 제어용 훅
export function useCounselStream({ sessionId, sseTicket }: UseCounselStreamProps) {
  const [aiContent, setAiContent] = useState<string>('');
  const [currentEmotion, setCurrentEmotion] = useState<'NEUTRAL' | 'HAPPY' | 'SAD' | 'ANGRY'>('NEUTRAL');
  const [isCompleted, setIsCompleted] = useState<boolean>(false);
  const [lastCompletedMessage, setLastCompletedMessage] = useState<SseMessageData | null>(null);

  useEffect(() => {
    if (!sessionId || !sseTicket) return;

    //쿼리 파라미터에 ticket을 실어 다이렉트 전송 주소 생성
    const url = `/api/sessions/${sessionId}/stream?ticket=${encodeURIComponent(sseTicket)}`;
    const eventSource = new EventSource(url);

    console.log('[SSE] 백엔드 스트림 서버와 연결을 시작합니다.');

    // ① 스트리밍 진행 중 (한 글자씩 오는 구역)
    eventSource.addEventListener('message-delta', (event) => {
      const parsed: SseMessageData = JSON.parse(event.data);
      // 기존 글자 조각 뒤에 새 글자를 부드럽게 붙여줍니다.
      setAiContent((prev) => prev + parsed.content);
      setCurrentEmotion(parsed.emotion);
    });

    // ② 한 문장 발화 생성 완전히 완료
    eventSource.addEventListener('message-done', (event) => {
      const parsed: SseMessageData = JSON.parse(event.data);
      console.log('✅ [SSE] AI 내담자 문장 생성 완료:', parsed.content);
      setLastCompletedMessage(parsed);
    });

    // ③ 서버 연동 처리 실패 에러 가동
    eventSource.addEventListener('message-error', (event) => {
      const parsed: SseErrorData = JSON.parse(event.data);
      alert(`[상담 에러]: ${parsed.message}`);
    });

    // ④ 오늘 상담 시뮬레이션 세션 완전히 폐쇄 (종료)
    eventSource.addEventListener('session-closed', (event) => {
      const parsed: SseCloseData = JSON.parse(event.data);
      console.log('🚫 [SSE] 세션이 정상 종료되었습니다. 사유:', parsed.reason);
      
      setIsCompleted(true);
      eventSource.close(); // 명세 지침 수동 해제 브레이크
    });

    eventSource.onerror = (error) => {
      console.error('❌ [SSE] 네트워크 단절 및 서버 에러:', error);
      // 브라우저 기본 엔진이 Last-Event-ID를 장착하여 자동 재연결을 가동합니다.
    };

    // 💡 [가장 안전한 무기: Cleanup] 사용자가 방을 나갈 때 연결 좀비 생존 영구 차단
    return () => {
      console.log('🔌 [SSE] 화면 탈출로 인해 실시간 스트림 연결을 완전 파기합니다.');
      eventSource.close();
    };
  }, [sessionId, sseTicket]);

  // 화면 컴포넌트단에 꿀맛처럼 쥐여줄 데이터와 상태 리스트들 반환
  return {
    aiContent,
    currentEmotion,
    isCompleted,
    lastCompletedMessage,
    clearChat: () => setAiContent(''),
  };
}