import React, { useContext, useEffect } from 'react';
import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import MediMindContext from '../context/MediMindContext';

//wrapper component that will check if login is still valid before showing page
function ProtectedRoute({ children }) {
	const [isChecking, setIsChecking] = useState(true); //loading state
	const [isLoggedIn, setIsLoggedIn] = useState(false);
	const mediMindCtx = useContext(MediMindContext);
	const { doctorDetails, setDoctorDetails } = mediMindCtx;

	//fetching GET API here to backend, checking if session exist for user, and then response
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
				const doctor = await response.json();
				if (response.ok) {
					setIsLoggedIn(true);
					setDoctorDetails(doctor);
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

	useEffect(() => {
		console.log('Doctor details updated:', doctorDetails);
	}, [doctorDetails]);
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
