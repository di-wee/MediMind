import React, {useEffect, useState} from 'react';

import doctorList from '../mockdata/doctorlist.json';
import {XCircleIcon} from "@heroicons/react/16/solid/index.js";

function DoctorDetails({ mcrNo }) {
	//1. need you to help with the update details mock functionality.
	//so on clicking update details, it will only allow
	//email address and practicing clinic to be updated.
	//and when u click the update button, it will change to a save button where on click it will
	//save the changes made
	//
	//2. this one not so important, but on button click of change password, it will come up a second
	//input field  [confirm password]  next to the password field
	// change password button will change to save password. on button click, it will save the changed password
	//
	//u will need to useState to manage all of this. jiayous! most important to finish feature 1, feature2 cannot nvm.
	const [doctorInfo, setDoctorInfo] = useState({
		firstName: '',
		lastName: '',
		mcrNo: '',
		emailAddress: '',
		clinicName: '',
		password: ''
	});
	const [validation,setValidation] = useState(false)
	const [isEditing,setIsEditing] = useState(false)
	const [isChangingPassword,setIsChangingPassword] = useState(false)
	const [password,setPassword] = useState(doctorInfo.password)
	const [email,setEmail]=useState(doctorInfo.emailAddress)
	const [clinic,setClinic]=useState(doctorInfo.clinicName)
	const [confirmPassword,setConfirmPassword]=useState('');
	const clinicOptions = [
		"Raffles Medical Centre",
		"Healthway Clinic",
		"Mount Elizabeth Medical",
		"Tan Tock Seng Hospital",
		"Singapore General Hospital"
	];

	const handleEditToggle = ()=>{
		if(isEditing){
			//call api to save
			setIsEditing(false);
		}else{
			setIsEditing(true);
		}
	}
	const handlePasswordToggle = ()=>{
		if (isChangingPassword){
			if(password==confirmPassword&&password&&confirmPassword){
				setValidation(false);
				setIsChangingPassword(false);
				//call api to save data
			}else{
				setValidation(true);
			}
		}else{
			setIsChangingPassword(true);
		}
	}

	useEffect(() => {
		const doctor = doctorList.find((d) => d.mcrNo===mcrNo);
		console.log(doctor)
		if(doctor){
			setDoctorInfo(doctor);
			setEmail(doctor.emailAddress || '');
			setClinic(doctor.clinicName || '');
			setPassword(doctor.password || '');
		}

	}, [mcrNo]);

	return (
		<>
			<main className='w-full flex-1 mt-23  bg-gray-50 min-h-screen'>
				{/* shadow-xl bg-white py-8 m-5 rounded-xl this is giving the shadow box effect */}

				<div className='border-1 border-gray-200 shadow-xl bg-white pt-8 m-5 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Account Details</h2>
					<div className='profile-details'>
						<div className='w-xl'>
							<label className='form-label'>First Name</label>
							<input
								className='form-input'
								type='text'
								name='firstName'
								value={doctorInfo.firstName}
								readOnly
							/>
						</div>
						<div className='w-xl '>
							<label className='form-label'>Last Name</label>
							<input
								className='form-input'
								type='text'
								name='lastName'
								value={doctorInfo.lastName}
								readOnly
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>MCR No</label>
							<input
								className='form-input'
								type='text'
								name='MCRNo'
								value={doctorInfo.mcrNo}
								readOnly
							/>
						</div>
					</div>
					<div className='profile-details pt-5 mb-15'>
						<div className='w-xl'>
							<label className='form-label'>Email Address</label>
							<input
								className='form-input'
								type='text'
								name='email'
								value={email}
								onChange={(e)=>setEmail(e.target.value)}
								readOnly={!isEditing}
							/>
						</div>
						<div className='w-xl'>
							<label className='form-label'>Practicing Clinic</label>
							{isEditing?(
								<select
									className='form-input'
									name='clinicName'
									value={clinic}
									onChange={(e) => setClinic(e.target.value)}
								>
									<option value="" disabled>-- Select Clinic --</option>
									{clinicOptions.map((name, index) => (
										<option key={index} value={name}>
											{name}
										</option>
									))}
								</select>
							):<input
								className='form-input'
								type='text'
								name='clinicName'
								value={clinic}
								readOnly
							/>}
						</div>
					</div>
				</div>
				<div className='border-1 border-gray-200 shadow-xl bg-white py-8 m-5 rounded-xl'>
					<h2 className='font-bold text-lg px-15 '>Password</h2>
					<div className='password-details flex flex-col gap-4 px-5'>
						<div className='w-full'>
							<label className='form-label'>Password</label>
							<input
								className='form-input'
								type='password'
								name='password'
								value={password}
								onChange={(e) => setPassword(e.target.value)}
								readOnly={!isChangingPassword}
							/>
						</div>

					{isChangingPassword && (
						<div className='w-full'>
							<label className='form-label'>Confirm Password</label>
							<input
								className='form-input'
								type='password'
								name='confirmPassword'
								value={confirmPassword}
								onChange={(e) => setConfirmPassword(e.target.value)}
							/>
						</div>
					)}
					{validation && (
						<div className='login-validation relative mt-2'>
							<p>password and confirm password don't matched!</p>
							<XCircleIcon
								className='size-4.5 absolute right-5 top-5 cursor-pointer'
								onClick={() => setValidation(false)}
							/>
						</div>
					)}

					<div className='w-1/2 mt-4'>
						<button onClick={handlePasswordToggle} className='btn-submit'>
							{isChangingPassword ? 'Save' : 'Change Password'}
						</button>
					</div>
					</div>
				</div>

				<div className='flex justify-end px-5 py-5'>
					<button onClick={handleEditToggle} className='btn-submit max-w-40 end'>
						{ isEditing ? 'Save':'Update Details'}
					</button>
				</div>
			</main>
		</>
	);
}

export default DoctorDetails;
