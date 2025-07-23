import React from 'react';
import { Navigate } from 'react-router';

//wrapper component that will check if login is still valid before showing page
function ProtectedRoute({ children }) {
	// temp logic, to replace with UseEffect once backend Auth API is developed
	const isLoggedIn = localStorage.getItem('isLoggedIn') === true;

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
