import './App.css';
import { Route, Routes } from 'react-router-dom';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';
import PatientProfile from './pages/PatientProfile';
import MedicationLog from './components/MedicationLog';
import LandingPage from './pages/LandingPage';
import AddPatientPage from './pages/AddPatientPage';
import Register from './pages/Register';
import DoctorProfile from './pages/DoctorProfile';
import MediMindContext from './context/MediMindContext';
import { useState } from 'react';

function App() {
	const [doctorDetails, setDoctorDetails] = useState({});
	return (
		<>
			<main>
				<MediMindContext.Provider value={{ doctorDetails, setDoctorDetails }}>
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
							path='/addpatient'
							element={
								<ProtectedRoute>
									<AddPatientPage />
								</ProtectedRoute>
							}
						/>

						{/* Add new patient page */}
						<Route
							path='/login'
							element={<Login />}
						/>

						<Route
							path='/profile/:mcrNo'
							element={
								<ProtectedRoute>
									<DoctorProfile />
								</ProtectedRoute>
							}
						/>
						<Route
							path='/register'
							element={<Register />}
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
					</Routes>
				</MediMindContext.Provider>
			</main>
		</>
	);
}

export default App;
