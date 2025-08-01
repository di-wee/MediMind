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

function App() {
	return (
		<>
			<main>
				<Routes>
					{/* Landing page: View & Remove Assigned Patients */}
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
						element={<DoctorProfile />}
					/>
					<Route
						path='/register'
						element={<Register />}
					/>

					{/* Login and Register */}
					<Route
						path='/login'
						element={<Login />}
					/>
					<Route
						path='/register'
						element={<Register />}
					/>

					{/* Patient profile and medication logs */}
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
