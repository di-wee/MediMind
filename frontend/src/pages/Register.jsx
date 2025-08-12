import { XCircleIcon } from '@heroicons/react/16/solid';
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/20/solid';
import React, { useContext, useRef, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import MediMindContext from '../context/MediMindContext';
import { API_BASE_URL } from '../utils/config';

function Register() {
	const [validation, setValidation] = useState({
		MCRNo: false,
		ConfirmPass: false,
		EmailDomain: false,
		DupeMCRNo: false,
	});
	const [passVisibility, setPassVisibility] = useState(false);
	const [confirmPassVisibility, setConfirmPassVisibility] = useState(false);
	const [clinicList, setClinicList] = useState([]);
	const mediMindCtx = useContext(MediMindContext);
	const { setCompletedSignUp } = mediMindCtx;

	const navigate = useNavigate();

	const mcrRef = useRef();
	const firstNameRef = useRef();
	const lastNameRef = useRef();
	const emailRef = useRef();
	const passwordRef = useRef();
	const confirmPassRef = useRef();
	const clinicRef = useRef();

	const handleSignUp = async (e) => {
		e.preventDefault();

		const form = e.target;

		//letting the inbuilt validation run first
		if (!form.checkValidity()) {
			form.reportValidity();
			return;
		}

		const mcrNo = mcrRef.current.value.trim();
		const firstName = firstNameRef.current.value.trim();
		const lastName = lastNameRef.current.value.trim();
		const email = emailRef.current.value.trim();
		const password = passwordRef.current.value;
		const confirmPass = confirmPassRef.current.value;
		const clinicName = clinicRef.current.value.trim();

		const errors = {
			MCRNo: mcrNo.length !== 7,
			ConfirmPass: password !== confirmPass,
		};

		setValidation(errors);

		//setting bool flag if any of the values in the KV of errors is true (means got error)
		const hasErrors = Object.values(errors).some((val) => val == true);
		if (hasErrors) return; // stopping logic if validation triggered

		try {
			const response = await fetch(API_BASE_URL + 'api/web/register', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
				},
				body: JSON.stringify({
					mcrNo,
					firstName,
					lastName,
					email,
					password,
					clinicName,
				}),
			});

			if (response.status === 400) {
				// parse the JSON error response to determine the specific error type
				try {
					const errorData = await response.json();
					if (errorData.error === 'duplicate_mcr') {
						setValidation((prev) => ({ ...prev, DupeMCRNo: true }));
					} else if (errorData.error === 'invalid_email_domain') {
						setValidation((prev) => ({ ...prev, EmailDomain: true }));
					} else {
						// default to email domain error for unknown 400 errors
						setValidation((prev) => ({ ...prev, EmailDomain: true }));
					}
				} catch {
					//// if we cannot parse the JSON, default to email domain error
					setValidation((prev) => ({ ...prev, EmailDomain: true }));
				}
				return;
			}

			if (response.ok) {
				setCompletedSignUp(true);
				navigate('/login', { replace: true });
			} else if (response.status === 404) {
				// handle clinic not found error
				try {
					const errorData = await response.json();
					if (errorData.error === 'clinic_not_found') {
						setValidation((prev) => ({ ...prev, EmailDomain: true }));
					}
				} catch {
					// if we cannot parse the JSON, default to email domain error
					setValidation((prev) => ({ ...prev, EmailDomain: true }));
				}
			} else {
				const errMsg = await response.text();
				console.error('Registration failed: ' + errMsg);
			}
		} catch (err) {
			console.error('Error with registration: ', err);
		}
		console.log('clinic: ', clinicName);
	};

	const handleSignIn = () => {
		navigate('/login', { replace: true });
	};

	const handleEmailChange = () => {
		// clear email domain validation when user types in email field
		if (validation.EmailDomain) {
			setValidation((prev) => ({ ...prev, EmailDomain: false }));
		}
	};

	const handleClinicChange = () => {
		// clear email domain validation when user changes clinic
		if (validation.EmailDomain) {
			setValidation((prev) => ({ ...prev, EmailDomain: false }));
		}
	};

	const handleMCRChange = () => {
		// clear duplicate MCR validation when user types in MCR field
		if (validation.DupeMCRNo) {
			setValidation((prev) => ({ ...prev, DupeMCRNo: false }));
		}
	};

	useEffect(() => {
		const fetchAllClinics = async () => {
			try {
				const response = await fetch(API_BASE_URL + 'api/web/all-clinics', {
					method: 'GET',
					headers: {
						'Content-Type': 'application/json',
					},
				});

				if (!response.ok) {
					throw new Error('Error retrieving clinics!');
				}

				const clinics = await response.json();
				setClinicList(clinics);
			} catch (err) {
				console.error(
					'Error exception caught when calling GET API to retrieve clinic: ',
					err
				);
			}
		};
		fetchAllClinics();
	}, []);

	return (
		<>
			<section>
				<div className='flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0 mt-20 '>
					<img
						src='/medimind_app_logo.png'
						className='size-20'></img>
					<h2 className='flex items-center  text-2xl font-semibold text-gray-800'>
						MediMind
					</h2>
					<div className='w-full max-w-2xl bg-white rounded-lg shadow-xl md:mt-0 sm:max-w-xl xl:p-0 mb-20'>
						<div className='p-6 space-y-4 md:space-y-6 sm:p-8'>
							<h2 className='text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl'>
								Create an account
							</h2>
							<form
								className='space-y-4 md:space-y-6'
								action='#'
								onSubmit={(e) => handleSignUp(e)}>
								<div className='flex items-center justify-between gap-10'>
									<div>
										<label
											htmlFor='firstName'
											className='form-label'>
											First Name
										</label>
										<input
											type='text'
											name='firstName'
											id='firstName'
											className='form-input'
											placeholder='John'
											ref={firstNameRef}
											required></input>
									</div>
									<div>
										<label
											htmlFor='lastName'
											className='form-label'>
											Last Name
										</label>
										<input
											type='text'
											name='lastName'
											id='lastName'
											className='form-input'
											placeholder='Doe'
											ref={lastNameRef}
											required></input>
									</div>
								</div>
								<div>
									<label
										htmlFor='mcrNo'
										className='form-label'>
										MCR Number
									</label>
									<input
										type='text'
										name='mcrNo'
										id='mcrNo'
										className='form-input'
										placeholder='M12345A'
										ref={mcrRef}
										onChange={handleMCRChange}
										required
									/>
									{validation['MCRNo'] && (
										<p className='inline-val-msg'>
											Please enter a valid MCR Number.
										</p>
									)}
									{validation['DupeMCRNo'] && (
										<p className='inline-val-msg'>
											An account has already been created with this MCR Number.
										</p>
									)}
								</div>
								<div>
									<label
										htmlFor='email'
										className='form-label'>
										Email Address
									</label>
									<input
										type='email'
										name='email'
										id='email'
										className='form-input'
										placeholder='name@company.com'
										ref={emailRef}
										onChange={handleEmailChange}
										required
									/>
									{validation['EmailDomain'] && (
										<p className='inline-val-msg'>
											Email is not verified for the specified clinic.
										</p>
									)}
								</div>
								<div>
									<label
										htmlFor='clinic'
										className='form-label'>
										Practicing Clinic
									</label>
									<select
										name='clinic'
										id='clinic'
										className='form-input'
										ref={clinicRef}
										onChange={handleClinicChange}
										required>
										<option
											value=''
											disabled
											selected>
											Select a clinic
										</option>
										{clinicList.map((clinic) => (
											<option
												key={clinic.id}
												value={clinic.clinicName}>
												{clinic.clinicName}
											</option>
										))}
									</select>
								</div>

								<div className='flex justify-between gap-10'>
									<div>
										<label
											htmlFor='password'
											className='form-label'>
											Password
										</label>
										<div>
											<input
												type={passVisibility ? 'text' : 'password'}
												name='password'
												id='password'
												placeholder='••••••••'
												className='form-input'
												minLength='8'
												ref={passwordRef}
												required
											/>
											{passVisibility ? (
												<EyeSlashIcon
													onClick={() => setPassVisibility(false)}
													className='size-5 relative left-40 bottom-8 cursor-pointer'
												/>
											) : (
												<EyeIcon
													onClick={() => setPassVisibility(true)}
													className='size-5 relative left-40 bottom-8 cursor-pointer'
												/>
											)}
										</div>
									</div>
									<div>
										<label
											htmlFor='confirmPassword'
											className='form-label'>
											Confirm Password
										</label>
										<div>
											<input
												type={confirmPassVisibility ? 'text' : 'password'}
												name='confirmPassword'
												id='confirmPassword'
												placeholder='••••••••'
												className='form-input'
												ref={confirmPassRef}
												required
											/>
											{confirmPassVisibility ? (
												<EyeSlashIcon
													onClick={() => setConfirmPassVisibility(false)}
													className='size-5 relative left-40 bottom-8 cursor-pointer'
												/>
											) : (
												<EyeIcon
													onClick={() => setConfirmPassVisibility(true)}
													className='size-5 relative left-40 bottom-8 cursor-pointer'
												/>
											)}

											{validation['ConfirmPass'] && (
												<p className='inline-val-msg'>
													Password does not match.
												</p>
											)}
										</div>
									</div>
								</div>
								<button
									type='submit'
									className='btn-submit'>
									Sign Up
								</button>
								<p className='text-sm font-light text-gray-500 text-center'>
									Already have an account?{' '}
									<a
										href='#'
										className='link'
										onClick={handleSignIn}>
										Sign in
									</a>
								</p>
							</form>
						</div>
					</div>
				</div>
			</section>
		</>
	);
}

export default Register;
