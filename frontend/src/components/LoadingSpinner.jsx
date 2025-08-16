import React from 'react';

const LoadingSpinner = ({ message = 'Loading...', size = 'medium' }) => {
	const sizeClasses = {
		small: 'h-6 w-6',
		medium: 'h-12 w-12',
		large: 'h-16 w-16',
	};

	return (
		<div className='flex flex-col items-center justify-center min-h-[200px]'>
			<div
				className={`animate-spin rounded-full border-b-2 border-gray-900 ${sizeClasses[size]} mb-4`}></div>
			<p className='text-gray-600 text-sm'>{message}</p>
		</div>
	);
};

export default LoadingSpinner;
