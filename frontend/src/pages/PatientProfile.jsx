import React from 'react';
import { useParams } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
function PatientProfile() {
	const { patientId } = useParams();
	return (
		<>
			<Sidebar />
			<Header />
			<PatientProfile />
		</>
	);
}

export default PatientProfile;
