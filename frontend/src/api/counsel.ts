import client from './client';
import { ApiEndpoints } from './endpoints';

export interface ChatMessage {
    id: string;
    from: 'client' | 'counselor';
    text: string;
}

export interface ClientInfo {
    clientId: number;
    clientName: string;
    tags: string[];
}

export interface MakeCounselsRequest {
    clientId : number;
	initialMessage : string;
	startAt : string;
}

export interface MakeCounselsResponse {
    success: boolean;
		message: string;
		data: {
			nickname : string;
			counselId : number;
			counselNo : number;
			sessionId : number;
			sessionRound : number;
			imageUrl : string;
			previousMemo : string | null;
			sseTicket : string;
			createdAt : string;
		}
}

export const makeCounsels = async (request: MakeCounselsRequest): Promise<MakeCounselsResponse> => {
    const response = await client.post(ApiEndpoints.makeCounsels, request);
    return response.data;
};

export interface NextCounselsRequest {
    initialMessage : string;
	startAt : string;
}

export interface NextCounselsResponse {
    success: boolean;
	message: string;
	data: {
		nickname : string;
		counselId : number;
		counselNo : number;
		sessionId : number;
		sessionRound : number;
		imageUrl : string;
		previousMemo : string;
		sseTicket : string;
		createdAt : string;
		}
}

export const nextCounsels = async (counselId: number, request: NextCounselsRequest): Promise<NextCounselsResponse> => {
    const response = await client.post(ApiEndpoints.nextCounsels(counselId), request);
    return response.data;
};

export interface SessionMessageRequest{
    message: string;
}

export interface SessionMessageResponse{
    success: boolean;
	message: string;
	data: null;
}

export const sessionMessage = async (sessionId: number, request: SessionMessageRequest): Promise<SessionMessageResponse> => {
    const response = await client.post(ApiEndpoints.sessionMessage(sessionId), request);
    return response.data;
};

export interface SessionCompleteRequest{
    memo : string | null;
	endAt : string;
}

export interface SessionCompleteResponse{
    success: boolean;
	message: string;
	data: {
		duration : number;
		isCounselCompleted : boolean;
	}
}

export const sessionComplete = async (sessionId: number, request: SessionCompleteRequest): Promise<SessionCompleteResponse> => {
    const response = await client.post(ApiEndpoints.sessionComplete(sessionId), request);
    return response.data;
};

export interface DelCounselResponse{
    success: boolean;
    message: string;
    data: null;
}

export const delCounsel = async (counselId: number): Promise<DelCounselResponse> => {
    const response = await client.delete(ApiEndpoints.delCounsel(counselId));
    return response.data;
};

export interface ViewSessionResponse{
    success: boolean;
    message: string;
    data: {
        nickname : string;
        counselNo : number;
        counselId : number;
        sessionRound : number;
        client: ClientInfo;
        duration : number;
        aiSummary : string;
        aiAdvice : string;
        transcriptId : number;
        reportId : number;
        createdAt : string;
    }	
}

export const viewSession = async (sessionId: number): Promise<ViewSessionResponse> => {
    const response = await client.get<ViewSessionResponse>(ApiEndpoints.viewSession(sessionId));
    return response.data;
}

export interface SessionListParams {
    idAfter?: number;
    limit?: number;
}

export interface SessionListResponse {
    success: boolean;
    message: string;
    data: {
        nickname : string;
        sessions : Array<
        {
            counselId : number;
            counselNo : number;
            sessionId : number;
            sessionRound : number;
            client: ClientInfo;
            duration : number;
            createdAt : string;
        }>
        nextCursor : number | null;
        hasNext : boolean;
    }
}

export const sessionList = async(params?: SessionListParams): Promise<SessionListResponse> => {
    const response = await client.get<SessionListResponse>(ApiEndpoints.sessionList, {params: params});
    return response.data;
}

export interface SessionItem {
    sessionId: number;
    sessionRound: number;
    duration: number;
}
export interface SpeechType {
    sessionRound: number;
    closedQuestion: number;
    openQuestion: number;
    simpleReflection: number;
    complexReflection: number;
    information: number;
    affirmation: number;
} 
export interface RatioItem {
    sessionRound: number;
    ratio: number;
}
export interface SpeakerTrend{
    sessionRound: number;
    counselorRatio: number;
    clientRatio: number;
}

export interface CounselFeedbackResponse {
    success: boolean;
    message: string;
    data: {
        nickname: string;
        counselNo: number;
        client: ClientInfo;
        totalDuration: number;
        sessions: SessionItem[];
        speechTypeTrend: SpeechType[];
        reflectionQuestionRatioTrend: RatioItem[];
        speakerRatioTrend: SpeakerTrend[];
    };
}

export const counselFeedback = async (counselId: number): Promise<CounselFeedbackResponse> => {
    const response = await client.get<CounselFeedbackResponse>(ApiEndpoints.counselFeedback(counselId));
    return response.data;
}

export interface ViewMemoResponse {
    success: boolean;
    message: string;
    data: {
        memo: string | null;
    }
}

export const viewMemo = async (sessionId: number): Promise<ViewMemoResponse> => {
    const response = await client.get<ViewMemoResponse>(ApiEndpoints.viewMemo(sessionId));
    return response.data;
}