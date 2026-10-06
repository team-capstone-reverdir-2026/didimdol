import client from './client';
import { ApiEndpoints } from './endpoints';

export interface ClientSummary {
    clientId: number;
    clientName: string;
    tags: string[];
    imageUrl: string;
    createdAt: string;
  }

export interface Client {
    clientId: number;
    clientName: string;
    tags: string[];
    imageUrl: string;
    referralReason: string;
    problemArea: string;
    counselingReason: string;
    createdAt: string;
}

export interface ClientListParams {
    idAfter?: number;
    limit?: number;
}

export interface ClientListResponse {
    success: boolean;
    message: string;
    data: {
        nickname : string;
        clients : Array<
        {
            clientId : number;
            clientName : string;
            tags : string[];
            imageUrl : string;
            createdAt : string;
        }>
        nextCursor : number | null;
        hasNext : boolean;
    }
}

export const clientList = async(params?: ClientListParams): Promise<ClientListResponse> => {
    const response = await client.get<ClientListResponse>(ApiEndpoints.clientList, {params: params});
    return response.data;
}

export interface ViewClientResponse {
    success: boolean,
    message: string,
	data: {
		nickname : string,
		clientId : number,
		clientName : string,
		age : number,
		job : string,
		tags : string[],
		imageUrl : string,
		referralReason : string,
		problemArea : string,
		counselingReason : string,
		createdAt : string
	}
}

export const viewClient = async (clientId: number): Promise<ViewClientResponse> => {
    const response = await client.get<ViewClientResponse>(ApiEndpoints.viewClient(clientId));
    return response.data;
}