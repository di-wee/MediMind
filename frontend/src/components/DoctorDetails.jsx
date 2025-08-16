import React, { useContext, useEffect, useState } from 'react';

import MediMindContext from '../context/MediMindContext.jsx';
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/20/solid';
import { API_BASE_URL } from '../utils/config';
import ConfirmationModal from './ConfirmationModal.jsx';
import LoadingSpinner from './LoadingSpinner.jsx';

function DoctorDetails({ mcrNo }) {
	const mediMindCtx = useContext(MediMindContext);
	const { doctorDetails, setDoctorDetails } = mediMindCtx;

	const [doctorInfo, setDoctorInfo] = useState(doctorDetails);

	const [isEditing, setIsEditing] = useState(false);
	const [isChangingPassword, setIsChangingPassword] = useState(false);
	const [password, setPassword] = useState(doctorInfo.password);
	const [currentPassVisibility, setCurrentPassVisibility] = useState(false);
	const [confirmPassVisibility, setConfirmPassVisibility] = useState(false);

	const [validation, setValidation] = useState({
		newPassValidation: false,
		currentPassValidation: false,
		emailDomain: false,
	});

	const [newPassword, setNewPassword] = useState('');
	const [clinicOptions, setClinicOptions] = useState([]);
	const [clinicLoading, setClinicLoading] = useState(true);
	const [showConfirmationModal, setShowConfirmationModal] = useState(false);
	const [originalClinic, setOriginalClinic] = useState(null);

	const handleEditToggle = async () => {
		//call api to save
		if (isEditing) {
			// check if clinic has changed
			const clinicChanged =
				originalClinic &&
				doctorInfo.clinic &&
				originalClinic.id !== doctorInfo.clinic.id;

			if (clinicChanged) {
				setShowConfirmationModal(true);
				return;
			}

			await performUpdate();
		} else {
			setIsEditing(true);
			setOriginalClinic(doctorInfo.clinic);
			// clear validation errors when starting to edit
			setValidation((prev) => ({ ...prev, emailDomain: false }));
		}
	};

	const performUpdate = async () => {
		try {
			const response = await fetch(API_BASE_URL + 'api/doctor/update', {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
				},
				body: JSON.stringify({
					mcrNo: doctorInfo.mcrNo,
					email: doctorInfo.email,
					clinic: doctorInfo.clinic,
				}),
			});

			if (response.status === 400) {
				// email domain validation failed
				setValidation((prev) => ({ ...prev, emailDomain: true }));
				return; // dont exit editing mode, let user fix the email
			}

			if (!response.ok) {
				const errMsg = await response.text();
				console.error('Update error: ', errMsg);
				setIsEditing(false); // exit editing mode for other errors
				return;
			}

			const updateDoctor = await response.json();
			setDoctorInfo(updateDoctor);
			setDoctorDetails(updateDoctor);
			// clear email domain validation error on successful update
			setValidation((prev) => ({ ...prev, emailDomain: false }));
			alert('Doctor info updated successfully!');
			setIsEditing(false); // exit editing mode on success
		} catch (error) {
			console.error('Update error:', error);
			setIsEditing(false); // exit editing mode for unexpected errors
		}
	};

	const handleConfirmClinicChange = () => {
		setShowConfirmationModal(false);
		performUpdate();
	};

	const handlePasswordToggle = async () => {
		if (isChangingPassword) {
			//if either field is empty don't save just revert
			if (password.length === 0 || newPassword.length === 0) {
				setNewPassword('');
				setPassword(doctorInfo.password);
				setIsChangingPassword(false);
				setValidation({
					currentPassValidation: false,
					newPassValidation: false,
				});
				setConfirmPassVisibility(false);
				setCurrentPassVisibility(false);
				return;
			}
			const currentPassValidation = password !== doctorInfo.password;
			const newPassValidation = newPassword.length < 6;

			setValidation({
				currentPassValidation,
				newPassValidation,
			});

			if (!currentPassValidation && !newPassValidation) {
				//save to db
				try {
					const response = await fetch(API_BASE_URL + 'api/doctor/update', {
						method: 'PUT',
						headers: {
							'Content-Type': 'application/json',
						},
						body: JSON.stringify({
							mcrNo: doctorInfo.mcrNo,
							password: newPassword,
						}),
					});
					if (!response.ok) {
						const errMsg = await response.text();
						alert('Failed to update doctor info: ' + errMsg);
						return;
					}
					const updateDoctor = await response.json();
					setPassword(newPassword);
					setDoctorInfo(updateDoctor);
					setDoctorDetails(updateDoctor);
					alert('Doctor info updated successfully!');
				} catch (error) {
					console.error('Update error:', error);
					alert('Server error');
				} finally {
					// reinitialise validation
					setValidation({
						currentPassValidation: false,
						newPassValidation: false,
					});
					setIsChangingPassword(false);
					setNewPassword('');
					setConfirmPassVisibility(false);
					setCurrentPassVisibility(false);
				}
			}
			console.log(password);
			console.log(doctorInfo.password);
		} else {
			setIsChangingPassword(true);
			setPassword('');
			console.log('currentpassword: ', doctorInfo.password);
		}
	};

	useEffect(() => {
		const fetchAllClinic = async () => {
			try {
				setClinicLoading(true);
				const response = await fetch(API_BASE_URL + 'api/web/all-clinics', {
					method: 'GET',
					headers: {
						'Content-Type': 'application/json',
					},
				});
				if (!response.ok) {
					throw new Error('Failed to fetch clinic list');
				}
				const clinicList = await response.json();
				console.log('📦 clinicList from API:', clinicList);
				setClinicOptions(clinicList);
			} catch (error) {
				console.error('Error loading clinics:', error);
			} finally {
				setClinicLoading(false);
			}
		};
		fetchAllClinic();
	}, [mcrNo]);

	return (
		<>
			<main className='w-full flex-1 bg-gray-50'>
				<div className='border-1 border-gray-200 shadow-xl bg-white pt-8 m-5 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Account Details</h2>
					<div className='profile-details'>
						<div className='w-xl'>
							<label className='form-label'>First Name</label>
							<input
								className='form-readonly'
								type='text'
								name='firstName'
								value={doctorInfo.firstName}
								disabled
							/>
						</div>
						<div className='w-xl '>
							<label className='form-label'>Last Name</label>
							<input
								className='form-readonly'
								type='text'
								name='lastName'
								value={doctorInfo.lastName}
								disabled
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>MCR No</label>
							<input
								className='form-readonly'
								type='text'
								name='MCRNo'
								value={doctorInfo.mcrNo}
								disabled
							/>
						</div>
					</div>
					<div className='profile-details pt-5 mb-15'>
						<div className='w-xl'>
							<label className='form-label'>Email Address</label>
							<input
								className={`form-editable ${!isEditing ? 'bg-gray-200' : ''}`}
								type='text'
								name='email'
								value={doctorInfo.email}
								onChange={(e) =>
									setDoctorInfo((prev) => ({ ...prev, email: e.target.value }))
								}
								disabled={!isEditing}
							/>
							{validation.emailDomain && (
								<p className='inline-val-msg'>
									Email is not verified for the specified clinic.
								</p>
							)}
						</div>
						<div className='w-xl'>
							<label className='form-label'>Practicing Clinic</label>
							{isEditing ? (
								clinicLoading ? (
									<div className='flex items-center justify-center h-10 bg-gray-100 rounded border'>
										<LoadingSpinner
											message='Loading clinics...'
											size='small'
										/>
									</div>
								) : (
									<select
										className='form-input'
										name='clinicName'
										value={doctorInfo.clinic?.id}
										onChange={(e) => {
											const selectedClinic = clinicOptions.find(
												(c) => c.id === e.target.value
											);
											if (selectedClinic) {
												setDoctorInfo((prev) => ({
													...prev,
													clinic: selectedClinic,
												}));
											}
										}}>
										<option
											value=''
											disabled>
											-- Select Clinic --
										</option>
										{clinicOptions.map((clinic) => (
											<option
												key={clinic.id}
												value={clinic.id}>
												{clinic.clinicName}
											</option>
										))}
									</select>
								)
							) : (
								<input
									className={`form-editable ${!isEditing ? 'bg-gray-200' : ''}`}
									type='text'
									name='clinicName'
									value={doctorInfo.clinic?.clinicName || ''}
									disabled
								/>
							)}
						</div>
					</div>
				</div>
				<div className='border-1 border-gray-200 shadow-xl bg-white py-8 m-5 mt-15 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Password</h2>
					<div className='password-details px-5'>
						<div className='w-1/2'>
							<label className='form-label'>
								{isChangingPassword ? 'Current Password' : 'Password'}
							</label>
							<div className='relative w-full'>
								<input
									className={`form-editable ${
										!isChangingPassword ? 'bg-gray-200' : 'relative'
									}`}
									type={currentPassVisibility ? 'text' : 'password'}
									name='password'
									value={password}
									onChange={(e) => setPassword(e.target.value)}
									disabled={!isChangingPassword}
								/>
								{isChangingPassword &&
									(currentPassVisibility ? (
										<EyeSlashIcon
											className='size-5 cursor-pointer absolute bottom-3 right-6'
											onClick={() => setCurrentPassVisibility(false)}
										/>
									) : (
										<EyeIcon
											className='size-5 cursor-pointer absolute bottom-3 right-6'
											onClick={() => setCurrentPassVisibility(true)}
										/>
									))}
							</div>
							{validation.currentPassValidation && (
								<p className='inline-val-msg'>
									Password does not match with current password.
								</p>
							)}
						</div>

						{isChangingPassword && (
							<div className='w-1/2'>
								<label className='form-label'>New Password</label>

								<div className='relative w-full'>
									<input
										className='form-editable'
										type={confirmPassVisibility ? 'text' : 'password'}
										name='newPassword'
										value={newPassword}
										onChange={(e) => setNewPassword(e.target.value)}
									/>
									{isChangingPassword &&
										(confirmPassVisibility ? (
											<EyeSlashIcon
												className='size-5 cursor-pointer absolute bottom-3 right-6'
												onClick={() => setConfirmPassVisibility(false)}
											/>
										) : (
											<EyeIcon
												className='size-5 cursor-pointer absolute bottom-3 right-6'
												onClick={() => setConfirmPassVisibility(true)}
											/>
										))}
								</div>
								{validation.newPassValidation && (
									<p className='inline-val-msg'>
										Password must be at least 6 characters long.
									</p>
								)}
							</div>
						)}
						<div className='w-50 flex items-end mt-4'>
							<button
								onClick={handlePasswordToggle}
								className='btn-submit'>
								{isChangingPassword ? 'Save' : 'Change Password'}
							</button>
						</div>
					</div>
				</div>

				<div className='flex justify-end px-5 py-5 mt-10'>
					<button
						onClick={handleEditToggle}
						className='btn-submit max-w-40 end'>
						{isEditing ? 'Save' : 'Update Details'}
					</button>
				</div>
			</main>

			<ConfirmationModal
				isOpen={showConfirmationModal} //controlling visibility of modal; if isOpen is false then it will return null
				onClose={() => setShowConfirmationModal(false)}
				onConfirm={handleConfirmClinicChange}
				title='Are you sure you want to change your clinic? Changing of clinic will unassign all patients.'
				confirmText='Yes, change clinic'
				cancelText='No, cancel'
			/>
		</>
	);
}

export default DoctorDetails;
