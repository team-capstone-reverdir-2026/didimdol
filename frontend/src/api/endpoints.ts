export const ApiEndpoints = {
    signIn: '/api/auth/sign-in',
    signOut: '/api/auth/sign-out',
    signUp: '/api/auth/sign-up',
    refresh: '/api/auth/refresh',

    makeCounsels: '/api/counsels',
    nextCounsels: (counselId: number) => `/api/counsels/${counselId}/sessions`,
    sessionStream: (sessionId: number) => `/api/sessions/${sessionId}/stream`,
    sessionMessage: (sessionId: number) => `/api/sessions/${sessionId}/messages`,
    sessionComplete: (sessionId: number) => `/api/sessions/${sessionId}/complete`,
    delCounsel: (counselId: number) => `/api/counsels/${counselId}`,
    viewSession: (sessionId: number) => `/api/sessions/${sessionId}`,
    sessionList: '/api/sessions',
    counselFeedback: (counselId: number) => `/api/counsels/${counselId}/feedback`,
    viewMemo: (sessionId: number) => `/api/sessions/${sessionId}/memo`,

    clientList: '/api/clients',
    viewClient: (clientId: number) => `/api/clients/${clientId}`,

    transcriptView: (transcriptId: number) => `/api/transcripts/${transcriptId}`,
    makeHighlight: (transcriptId: number) => `/api/transcripts/${transcriptId}/highlights`,
    delHighlight: (highlightId: number) => `/api/highlights/${highlightId}`,

    viewReport: (reportId: number) => `/api/reports/${reportId}`,

    publicPaths: ['/api/auth/sign-in', '/api/auth/refresh', '/api/auth/sign-up'],
    isPublicPath(path: string | undefined): boolean {
        if (!path) return false;
        return this.publicPaths.includes(path);
    }

}