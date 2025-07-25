import { useState } from 'react';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import './App.css';
import { Route, Link, Routes } from 'react-router-dom';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';
import PatientProfile from './pages/PatientProfile';
import MedicationLog from './pages/MedicationLog';
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

					{/* kiv again if this route is required or we can just use medicine log
					as a separate component to be reused in PatientProfile component*/}
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
