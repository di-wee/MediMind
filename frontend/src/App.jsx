import { useState } from 'react';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import './App.css';
import { Route, Link, Routes } from 'react-router';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';
import PatientProfile from './pages/PatientProfile';
import MedicationLog from './pages/MedicationLog';
import LandingPage from './pages/LandingPage';

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
						path='/patient/:patientId'
						element={
							<ProtectedRoute>
								<PatientProfile />
							</ProtectedRoute>
						}
					/>
					<Route
						path='/patient/:patientId/:medicationId/log'
						element={
							<ProtectedRoute>
								<MedicationLog />
							</ProtectedRoute>
						}
					/>
				</Routes>
			</main>
		</>
	);
}

export default App;
