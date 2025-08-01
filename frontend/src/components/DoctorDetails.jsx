import React, { useEffect, useRef, useState } from 'react';

import doctorList from '../mockdata/doctorlist.json';
import { XCircleIcon } from '@heroicons/react/16/solid/index.js';

function DoctorDetails({ mcrNo }) {
	const [doctorInfo, setDoctorInfo] = useState({
		firstName: '',
		lastName: '',
		mcrNo: '',
		emailAddress: '',
		clinicName: '',
		password: '',
	});

	const [isEditing, setIsEditing] = useState(false);
	const [isChangingPassword, setIsChangingPassword] = useState(false);
	const [password, setPassword] = useState(doctorInfo.password);
	const [email, setEmail] = useState(doctorInfo.emailAddress);
	const [clinic, setClinic] = useState(doctorInfo.clinicName);
	const [validation, setValidation] = useState({
		newPassValidation: false,
		currentPassValidation: false,
	});

	const [newPassword, setNewPassword] = useState('');
	const clinicOptions = [
		'Raffles Medical Centre',
		'Healthway Clinic',
		'Mount Elizabeth Medical',
		'Tan Tock Seng Hospital',
		'Singapore General Hospital',
	];

	const handleEditToggle = () => {
		if (isEditing) {
			//call api to save
			setIsEditing(false);
		} else {
			setIsEditing(true);
		}
	};
	const handlePasswordToggle = () => {
		if (isChangingPassword) {
			//if either field is empty dont save just revert
			if (password.length === 0 || newPassword.length === 0) {
				setPassword(doctorInfo.password); // reset to original
				setNewPassword('');
				setIsChangingPassword(false);
				setValidation({
					currentPassValidation: false,
					newPassValidation: false,
				});
				return;
			}
			const currentPassValidation = password !== doctorInfo.password;
			const newPassValidation = newPassword.length < 6;

			setValidation({
				currentPassValidation,
				newPassValidation,
			});

			if (!currentPassValidation && !newPassValidation) {
				setDoctorInfo((prev) => ({ ...prev, password: newPassword }));
				setPassword(newPassword);
				setNewPassword('');
				setIsChangingPassword(false);
			}
			console.log(password);
			console.log(doctorInfo.password);
			return;
		}

		// reinitialise validation
		setValidation({ currentPassValidation: false, newPassValidation: false });
		setIsChangingPassword(true);
	};

	useEffect(() => {
		const doctor = doctorList.find((d) => d.mcrNo === mcrNo);
		console.log(doctor);
		if (doctor) {
			setDoctorInfo(doctor);
			setEmail(doctor.emailAddress || '');
			setClinic(doctor.clinicName || '');
			setPassword(doctor.password || '');
		}
	}, [mcrNo]);

	return (
		<>
			<main className='w-full flex-1 bg-gray-50 min-h-screen'>
				{/* shadow-xl bg-white py-8 m-5 rounded-xl this is giving the shadow box effect */}

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
								value={email}
								onChange={(e) => setEmail(e.target.value)}
								disabled={!isEditing}
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>Practicing Clinic</label>
							{isEditing ? (
								<select
									className='form-input'
									name='clinicName'
									value={clinic}
									onChange={(e) => setClinic(e.target.value)}>
									<option
										value=''
										disabled>
										-- Select Clinic --
									</option>
									{clinicOptions.map((name, index) => (
										<option
											key={index}
											value={name}>
											{name}
										</option>
									))}
								</select>
							) : (
								<input
									className={`form-editable ${!isEditing ? 'bg-gray-200' : ''}`}
									type='text'
									name='clinicName'
									value={clinic}
									disabled
								/>
							)}
						</div>
					</div>
				</div>
				<div className='border-1 border-gray-200 shadow-xl bg-white py-8 m-5 mt-10 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Password</h2>
					<div className='password-details px-5'>
						<div className='w-1/2'>
							<label className='form-label'>
								{isChangingPassword ? 'Current Password' : 'Password'}
							</label>
							<input
								className={`form-editable ${
									!isChangingPassword ? 'bg-gray-200' : ''
								}`}
								type='password'
								name='password'
								value={password}
								onChange={(e) => setPassword(e.target.value)}
								disabled={!isChangingPassword}
							/>
							{validation.currentPassValidation && (
								<p className='inline-val-msg'>
									Password does not match with current password.
								</p>
							)}
						</div>

						{isChangingPassword && (
							<div className='w-1/2'>
								<label className='form-label'>New Password</label>
								<input
									className='form-editable'
									type='password'
									name='newPassword'
									value={newPassword}
									onChange={(e) => setNewPassword(e.target.value)}
								/>
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

				<div className='flex justify-end px-5 py-5'>
					<button
						onClick={handleEditToggle}
						className='btn-submit max-w-40 end'>
						{isEditing ? 'Save' : 'Update Details'}
					</button>
				</div>
			</main>
		</>
	);
}

export default DoctorDetails;
