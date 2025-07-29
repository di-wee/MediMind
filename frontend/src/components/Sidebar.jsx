import {
	ArrowLeftStartOnRectangleIcon,
	Squares2X2Icon,
	UserCircleIcon,
} from '@heroicons/react/24/outline';
import React from 'react';
import { Link, useLocation } from 'react-router-dom';

function Sidebar({ mcrNo, firstName }) {
	const location = useLocation();

	const handleLogout = () => {
		localStorage.removeItem('isLoggedIn');
	};

	const isActive = (path) => {
		return location.pathname === path;
	};

	return (
		<div className='fixed top-0 left-0 h-screen w-64 bg-white border-r border-gray-300 shadow-lg z-20'>
			<aside className='flex flex-col h-full'>
				{/* Header */}
				<div className='px-6 py-8 border-b border-gray-200 bg-gray-50'>
					<div className='text-center mb-6'>
						<h1 className='text-2xl font-bold text-gray-800 tracking-tight'>
							MediMind
						</h1>
						<div className='w-12 h-0.5 bg-gray-400 mx-auto mt-2'></div>
					</div>
					
					{/* Doctor Profile */}
					<div className='flex items-center space-x-3 p-3 bg-white rounded-lg shadow-sm border border-gray-100'>
						{/* Profile Photo Placeholder */}
						<div className='w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center border-2 border-gray-300 flex-shrink-0'>
							<UserCircleIcon className='w-8 h-8 text-gray-400' />
						</div>
						<div className='flex-1 min-w-0'>
							<p className='text-sm font-semibold text-gray-900 truncate'>
								Dr. {firstName}
							</p>
							<p className='text-xs text-gray-500 truncate'>
								MCR: {mcrNo}
							</p>
						</div>
					</div>
				</div>

				{/* Navigation */}
				<nav className='flex-1 px-4 py-6'>
					<div className='space-y-1'>
						<Link
							to={`/profile/${mcrNo}`}
							className={`group flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200 ${
								isActive(`/profile/${mcrNo}`)
									? 'bg-gray-100 text-gray-900 border-l-4 border-gray-600'
									: 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
							}`}
						>
							<UserCircleIcon className={`w-5 h-5 mr-3 transition-colors ${
								isActive(`/profile/${mcrNo}`) ? 'text-gray-700' : 'text-gray-400 group-hover:text-gray-600'
							}`} />
							Account
						</Link>
						
						<Link
							to='/'
							className={`group flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200 ${
								isActive('/')
									? 'bg-gray-100 text-gray-900 border-l-4 border-gray-600'
									: 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
							}`}
						>
							<Squares2X2Icon className={`w-5 h-5 mr-3 transition-colors ${
								isActive('/') ? 'text-gray-700' : 'text-gray-400 group-hover:text-gray-600'
							}`} />
							Assigned Patients
						</Link>
					</div>
				</nav>

				{/* Footer */}
				<div className='p-4 border-t border-gray-200 bg-gray-50'>
					<Link
						to='/login'
						onClick={handleLogout}
						className='group flex items-center px-4 py-3 text-sm font-medium text-gray-600 rounded-lg hover:bg-white hover:text-red-600 hover:shadow-sm transition-all duration-200 w-full border border-transparent hover:border-gray-200'
					>
						<ArrowLeftStartOnRectangleIcon className='w-5 h-5 mr-3 text-gray-400 group-hover:text-red-500 transition-colors' />
						Logout
					</Link>
				</div>
			</aside>
		</div>
	);
}

export default Sidebar;