import { useState } from 'react';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import './App.css';
import { Route, Link, Routes } from 'react-router-dom';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';
import PatientProfile from './pages/PatientProfile';
import MedicationLog from './components/MedicationLog';
import LandingPage from './pages/LandingPage';
import Register from './pages/Register';

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
					<Route
						path='/register'
						element={<Register />}
					/>
					<Route
						path='/patient/:patientId'
						element={
							<ProtectedRoute>
								<PatientProfile />
							</ProtectedRoute>
						}
					/>
				</Routes>
			</main>
		</>
	);
}

export default App;
