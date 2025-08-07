import React, { useState } from 'react';
import {
	MagnifyingGlassIcon,
	UserPlusIcon,
	ArrowLeftIcon,
} from '@heroicons/react/24/outline';
function UnassignPatientList({
	setSearchTerm,
	searchTerm,
	displayList,
	handleAssign,
	unassignedPatients,
}) {
	const [currentPage, setCurrentPage] = useState(1);
	const patientsPerPage = 5;

	const indexOfLastPt = currentPage * patientsPerPage;
	const indexOfFirstPt = indexOfLastPt - patientsPerPage;
	// slicing to get the patients for that particular page
	const currentPt = displayList.slice(indexOfFirstPt, indexOfLastPt);

	const totalPages = Math.ceil(displayList.length / patientsPerPage);
	//getting the number of pages
	const pageNumbers = [];
	for (let i = 1; i <= totalPages; i++) {
		pageNumbers.push(i);
	}

	return (
		<>
			<div className='flex-1 p-8 px-15 bg-gray-50 min-h-screen w-full pt-10'>
				{/* Back Button */}
				<div className='mb-6'>
					<button
						onClick={() => (window.location.href = '/')}
						className='inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors duration-200'>
						<ArrowLeftIcon className='w-4 h-4 mr-2' />
						Back to Patients
					</button>
				</div>

				{/* Search Bar */}
				<div className='mb-6'>
					<div className='relative max-w-md'>
						<div className='absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none'>
							<MagnifyingGlassIcon className='h-5 w-5 text-gray-400' />
						</div>
						<input
							type='text'
							className='block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-2 focus:ring-gray-400 focus:border-transparent text-sm'
							placeholder='Search by name, NRIC...'
							value={searchTerm}
							onChange={(e) => setSearchTerm(e.target.value)}
						/>
					</div>
				</div>

				{/* Patient List */}
				<div className='bg-white rounded-lg shadow-sm border border-gray-200'>
					{currentPt.length === 0 ? (
						<div className='p-8 text-center'>
							<UserPlusIcon className='mx-auto h-12 w-12 text-gray-400 mb-4' />
							<h3 className='text-lg font-medium text-gray-900 mb-2'>
								No patients found
							</h3>
							<p className='text-gray-500'>
								{searchTerm
									? 'Try adjusting your search terms.'
									: 'No unassigned patients available.'}
							</p>
						</div>
					) : (
						<div className='divide-y divide-gray-200'>
							{currentPt.map((patient) => (
								<div
									key={patient.id}
									className='p-6 hover:bg-gray-50 transition-colors duration-150'>
									<div className='flex items-center justify-between'>
										<div className='flex items-center space-x-4'>
											{/* Patient Avatar Placeholder */}
											<div className='w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center border border-gray-300 flex-shrink-0'>
												<span className='text-sm font-semibold text-gray-600'>
													{patient.firstName.charAt(0)}
													{patient.lastName.charAt(0)}
												</span>
											</div>

											{/* Patient Info */}
											<div>
												<div className='flex items-center space-x-3 mb-1'>
													<h3 className='text-lg font-semibold text-gray-900'>
														{patient.firstName} {patient.lastName}
													</h3>
													{/* Gender Badge */}
													<span
														className={`inline-flex items-center justify-center w-6 h-6 rounded-full text-xs font-bold text-white ${
															patient.gender === 'Male'
																? 'bg-blue-500'
																: 'bg-pink-500'
														}`}>
														{patient.gender === 'Male' ? 'M' : 'F'}
													</span>
												</div>
												<div className='flex items-center space-x-4 text-sm text-gray-500 mt-1'>
													<span>NRIC: ****{patient.nric.slice(-4)}</span>
													<span>•</span>
													<span>DOB: {patient.dob}</span>
													<span>•</span>
													<span>{patient.clinic.clinicName}</span>
												</div>
											</div>
										</div>

										{/* Assign Button */}
										<button
											onClick={() => handleAssign(patient.id)}
											className='inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors duration-200'>
											<UserPlusIcon className='w-4 h-4 mr-2' />
											Assign
										</button>
									</div>
								</div>
							))}
						</div>
					)}
				</div>
				{totalPages > 1 && (
					<div className='flex justify-center mt-6 space-x-2'>
						{pageNumbers.map((page) => (
							<button
								key={page}
								onClick={() => setCurrentPage(page)}
								className={`px-3 py-1 text-sm border rounded ${
									currentPage === page
										? 'bg-gray-800 text-white'
										: 'bg-white text-gray-800'
								} hover:bg-gray-100 transition`}>
								{page}
							</button>
						))}
					</div>
				)}

				{/* Summary */}
				{displayList.length > 0 && (
					<div className='mt-6 bg-white rounded-lg border border-gray-200 p-4'>
						<p className='text-sm text-gray-600'>
							Showing {indexOfFirstPt + 1}–
							{Math.min(indexOfLastPt, displayList.length)} of{' '}
							{displayList.length} unassigned patients
						</p>
					</div>
				)}
			</div>
		</>
	);
}

export default UnassignPatientList;
