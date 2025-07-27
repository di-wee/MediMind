import {
	ArrowLeftStartOnRectangleIcon,
	Squares2X2Icon,
	UserCircleIcon,
} from '@heroicons/react/20/solid';
import React from 'react';
import { Link } from 'react-router-dom';

function Sidebar({ mcrNo, firstName }) {
	const handleLogout = () => {
		localStorage.removeItem('isLoggedIn');
	};

	return (
		<>
			<div className='flex h-screen m-0 p-0 fixed z-10 overflow-x-hidden top-0 left-0 shadow-xl flex-shrink'>
				<aside className='w-64 bg-white text-gray-800 p-6 space-y-8 flex flex-col  h-screen'>
					<div>
						<h2 className='text-2xl font-bold mb-8 text-center text-gray-700 mt-5'>
							<span className='flex justify-center items-center'>
								<img
									className='w-8 h-8 mr-2'
									src='https://flowbite.s3.amazonaws.com/blocks/marketing-ui/logo.svg'
									alt='logo'
								/>
								MediMind
							</span>
						</h2>
						<hr className='border-t border-gray-300'></hr>
						<div>
							<h2 className='font-semibold text-center mb-10 mt-5'>
								Welcome, Dr. {firstName}
							</h2>
						</div>

						<nav className='flex flex-col space-y-4'>
							<Link
								className='nav-button'
								to={`/profile/${mcrNo}`}>
								<UserCircleIcon className='size-6 mr-2 text-gray-500' /> Account
							</Link>
							<Link
								className='nav-button'
								to='/'>
								<Squares2X2Icon className='size-6 mr-2 text-gray-500' />{' '}
								Assigned Patients
							</Link>
						</nav>
					</div>
					<div className='grid grid-flow-col grid-rows-10 '>
						<Link
							className='nav-button w-full text-left row-start-10'
							style={{ fontSize: '15px' }}
							onClick={() => handleLogout()}
							to='/login'>
							<ArrowLeftStartOnRectangleIcon className='size-4 mr-2 text-gray-500' />
							Logout
						</Link>
					</div>
				</aside>
			</div>
		</>
	);
}

export default Sidebar;
