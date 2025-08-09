// Configuration utility for handling environment variables
export const getApiBaseUrl = () => {
	// Check if VITE_SERVER is available (build-time)
	if (import.meta.env.VITE_SERVER) {
		return import.meta.env.VITE_SERVER;
	}

	// Fallback: try to get from window.location (runtime)
	if (typeof window !== 'undefined') {
		const protocol = window.location.protocol;
		const hostname = window.location.hostname;
		const port = '8080'; // Backend port
		return `${protocol}//${hostname}:${port}/`;
	}

	// Default fallback
	return 'http://localhost:8080/';
};

export const API_BASE_URL = getApiBaseUrl();
