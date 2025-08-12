import React from 'react';
import {
	XMarkIcon,
	ExclamationTriangleIcon,
} from '@heroicons/react/24/outline';

function ConfirmationModal({
	isOpen,
	onClose,
	onConfirm,
	title = 'Are you sure you want to delete this product?',
	confirmText = "Yes, I'm sure",
	cancelText = 'No, cancel',
}) {
	if (!isOpen) return null;

	return (
		<div
			id='popup-modal'
			tabIndex='-1'
			className='fixed inset-0 z-50 flex items-center justify-center p-4'>
			<div className='relative w-full max-w-md'>
				<div className='relative bg-white rounded-lg shadow-2xl border-gray-400 border-1'>
					<button
						type='button'
						className='absolute cursor-pointer top-3 end-2.5 text-gray-800 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm w-8 h-8 ms-auto inline-flex justify-center items-center'
						onClick={onClose}>
						<XMarkIcon className='w-5 h-5' />
						<span className='sr-only'>Close modal</span>
					</button>
					<div className='flex flex-col p-4 md:p-5 text-center'>
						<ExclamationTriangleIcon className='mx-auto mb-4 text-gray-800 w-12 h-12' />
						<h3 className='mb-5 text-lg font-normal text-gray-800'>{title}</h3>
						<div className='flex flex-row justify-between mt-5'>
							<button
								type='button'
								className='text-white bg-green-700 hover:bg-green-950 focus:ring-4 focus:outline-none focus:ring-green-300 focus:ring-green-800 font-medium rounded-lg text-sm inline-flex items-center px-5 py-2.5 text-center'
								onClick={onConfirm}>
								{confirmText}
							</button>
							<button
								type='button'
								className='py-2.5 px-5 ms-3 text-sm font-medium text-white focus:outline-none bg-red-600 rounded-lg border border-gray-200 hover:bg-red-950 focus:z-10 focus:ring-4 focus:ring-red-100 '
								onClick={onClose}>
								{cancelText}
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	);
}

export default ConfirmationModal;
