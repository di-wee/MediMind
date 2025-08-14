import React, { useEffect, useState } from 'react';
import MedicationList from './MedicationList';
import { API_BASE_URL } from '../utils/config';
import LoadingSpinner from './LoadingSpinner';

function PatientDetails({ patientId }) {
	const [patientInfo, setPatientInfo] = useState({});
	const [medicationList, setMedicationList] = useState([]);
	const [loading, setLoading] = useState(true);
	const [medicationLoading, setMedicationLoading] = useState(true);

	//call GET API to retrieve patient's information
	useEffect(() => {
		const fetchPatientDetails = async () => {
			try {
				setLoading(true);
				console.log('PatientDetails mounted with patientId:', patientId);
				const response = await fetch(
					API_BASE_URL + `api/patient/${patientId}`,
					{
						method: 'GET',
						headers: {
							'Content-Type': 'application/json',
						},
					}
				);

				if (response.ok) {
					const patient = await response.json();
					console.log(patient);

					// compute derived fields
					const fullName = `${patient.firstName} ${patient.lastName}`;
					const dob = new Date(patient.dob);
					const today = new Date();

					let age = today.getFullYear() - dob.getFullYear();
					//if patient's birthday hasnt passed, then to keep them a year younger
					if (
						today.getMonth() < dob.getMonth() ||
						(today.getMonth() === dob.getMonth() &&
							today.getDate() < dob.getDate())
					) {
						age--;
					}

					setPatientInfo({
						...patient,
						fullName,
						age,
					});
				}
			} catch (err) {
				console.error('Error fetching patient details: ', err);
			} finally {
				setLoading(false);
			}
		};

		const fetchPatientMedicationList = async () => {
			try {
				setMedicationLoading(true);
				const response = await fetch(
					API_BASE_URL + `api/patient/${patientId}/medications`,
					{
						method: 'GET',
						headers: {
							'Content-Type': 'application/json',
						},
					}
				);

				if (response.ok) {
					const medication = await response.json();
					setMedicationList(medication);
				}
			} catch (err) {
				console.error('Error in retrieving medication list of patient: ', err);
			} finally {
				setMedicationLoading(false);
			}
		};
		fetchPatientDetails();
		fetchPatientMedicationList();
	}, [patientId]);

	if (loading) {
		return (
			<main className='w-full flex-1 bg-gray-50 min-h-screen'>
				<LoadingSpinner message='Loading patient details...' />
			</main>
		);
	}

	return (
		<>
			<main className='w-full flex-1 bg-gray-50 min-h-screen'>
				<div className='shadow-xl bg-white py-8 m-5 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Patient Details</h2>
					<div className='patient-details'>
						<div className='w-xl'>
							<label className='form-label'>Patient's Name</label>
							<input
								className='form-input'
								type='text'
								name='patientName'
								value={patientInfo.fullName || ''}
								readOnly
							/>
						</div>
						<div className='w-xl '>
							<label className='form-label'>Patient's NRIC</label>
							<input
								className='form-input'
								type='text'
								name='patientNRIC'
								value={patientInfo.nric || ''}
								readOnly
							/>
						</div>
					</div>
					<div className='patient-details mb-5'>
						<div className='w-xl'>
							<label className='form-label'>Patient's DOB</label>
							<input
								className='form-input'
								type='text'
								name='patientDob'
								value={patientInfo.dob || ''}
								readOnly
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>Patient's Age</label>
							<input
								className='form-input'
								type='text'
								name='patientAge'
								value={patientInfo.age ?? ''}
								readOnly
							/>
						</div>
					</div>
				</div>
				<div className='mt-8'>
					{medicationLoading ? (
						<div className='shadow-xl bg-white py-8 m-5 rounded-xl'>
							<LoadingSpinner
								message='Loading medications...'
								size='small'
							/>
						</div>
					) : (
						<MedicationList
							patientId={patientId}
							medicationList={medicationList}
							setMedicationList={setMedicationList}
						/>
					)}
				</div>
			</main>
		</>
	);
}

export default PatientDetails;
