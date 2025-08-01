import React, { useEffect } from 'react';
import { useState } from 'react';
import { Navigate } from 'react-router-dom';

//wrapper component that will check if login is still valid before showing page
function ProtectedRoute({ children }) {
	const [isChecking, setIsChecking] = useState(true); //loading state
	const [isLoggedIn, setIsLoggedIn] = useState(false);

	useEffect(() => {
		const checkSession = async () => {
			try {
				const response = await fetch(
					import.meta.env.VITE_SERVER + 'api/web/session-info',
					{
						method: 'GET',
						credentials: 'include',
					}
				);

				if (response.ok) {
					setIsLoggedIn(true);
				} else {
					setIsLoggedIn(false);
				}
			} catch (err) {
				console.error('Error checking session: ', err);
				setIsLoggedIn(false);
			} finally {
				setIsChecking(false);
			}
		};
		checkSession();
	}, []);
	if (isChecking) {
		return <div>Loading...</div>;
	}

	if (!isLoggedIn) {
		return (
			<Navigate
				to='/login'
				replace
			/>
		);
	}

	return children;
}

export default ProtectedRoute;
