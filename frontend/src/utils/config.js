// Configuration utility for handling environment variables
export const getApiBaseUrl = () => {
	// Check if VITE_SERVER is available (build-time)
	const viteServer = import.meta.env.VITE_SERVER;
	console.log('VITE_SERVER from env:', viteServer);

	if (viteServer && viteServer !== 'undefined' && viteServer !== '') {
		console.log('Using VITE_SERVER from environment:', viteServer);
		return viteServer;
	}

	// Fallback: try to get from window.location (runtime)
	if (typeof window !== 'undefined' && window.location) {
		const protocol = window.location.protocol;
		const hostname = window.location.hostname;
		const port = '8080'; // Backend port

		console.log('Window location:', { protocol, hostname, port });

		// Handle EC2 deployment scenario - check for EC2 IP or domain
		if (
			hostname === '54.179.144.79' ||
			hostname.includes('ec2') ||
			hostname.includes('amazonaws')
		) {
			const runtimeUrl = `${protocol}//${hostname}:${port}/`;
			console.log('Using EC2 runtime URL:', runtimeUrl);
			return runtimeUrl;
		}

		// Handle local development
		const runtimeUrl = `${protocol}//${hostname}:${port}/`;
		console.log('Using runtime URL:', runtimeUrl);
		return runtimeUrl;
	}

	// Default fallback for server-side rendering or when window is not available
	console.log('Using default URL: http://localhost:8080/');
	return 'http://localhost:8080/';
};

// Calculate the API_BASE_URL
export const API_BASE_URL = getApiBaseUrl();

// Runtime configuration function (for future use)
export const getRuntimeConfig = async () => {
	try {
		// Try to fetch a runtime config file
		const response = await fetch('/config.json');
		if (response.ok) {
			const config = await response.json();
			console.log('Runtime config loaded:', config);
			return config.apiBaseUrl || API_BASE_URL;
		}
	} catch (error) {
		console.log('No runtime config found, using default:', error.message);
	}
	return API_BASE_URL;
};
