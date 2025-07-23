import { useState } from 'react';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import './App.css';
import { Route, Link, Routes } from 'react-router-dom';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
	return (
		<>
			<main>
				<Routes>
					<Route
						path='/'
						element={
							<ProtectedRoute>
								<LandingPage />
							</ProtectedRoute>
						}
					/>
					<Route
						path='/login'
						element={<Login />}
					/>
				</Routes>
			</main>
		</>
	);
}

export default App;
