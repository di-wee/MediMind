import React from 'react';
import { useNavigate } from 'react-router-dom';

function Login() {
	const navigate = useNavigate();

	const handleSignUp = (e) => {
		e.preventDefault();
		navigate('/register', { replace: true });
	};

	return (
		<>
			<section>
				<div className='flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0'>
					<a
						href='#'
						className='flex items-center mb-6 text-2xl font-semibold text-gray-900'>
						<img
							className='w-8 h-8 mr-2'
							src='https://flowbite.s3.amazonaws.com/blocks/marketing-ui/logo.svg'
							alt='logo'
						/>
						MediMind
					</a>
					<div className='w-96 bg-white rounded-lg shadow-xl md:mt-0 sm:max-w-md xl:p-0'>
						<div className='p-6 space-y-4 md:space-y-6 sm:p-8'>
							<h2 className='text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl'>
								Welcome back!
							</h2>
							<form
								className='space-y-4 md:space-y-6'
								action='#'>
								<div>
									<label
										htmlFor='mcrNo'
										className='block mb-2 text-sm font-medium text-gray-900'>
										MCR Number
									</label>
									<input
										type='text'
										name='mcrNo'
										id='mcrNo'
										className='bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5'
										placeholder='M1234567A'
										required
									/>
								</div>
								<div>
									<label
										htmlFor='password'
										className='block mb-2 text-sm font-medium text-gray-900'>
										Password
									</label>
									<input
										type='password'
										name='password'
										id='password'
										placeholder='••••••••'
										className='bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5'
										required
									/>
								</div>
								<div className='flex items-center justify-center'>
									<a
										href='#'
										className='link'>
										Forgot password?
									</a>
								</div>
								<button
									type='submit'
									className='btn-submit'>
									Sign in
								</button>
								<p className='text-sm font-light text-gray-500'>
									Don’t have an account yet?{' '}
									<a
										href='#'
										className='link'
										onClick={(e) => handleSignUp(e)}>
										Sign up
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

export default Login;
