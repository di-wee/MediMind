import { TrashIcon, EyeIcon } from '@heroicons/react/24/outline';
import { PlusIcon } from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';

export default function PatientListView({
	patients,
	onRemove,
	searchTerm,
	onAddPatient,
	sortBy,
	onSort,
}) {
	const navigate = useNavigate();

	const handleViewDetails = (patientId) => {
		navigate(`/patient/${patientId}`, { replace: true });
	};

	const handleSort = (field) => {
		if (sortBy === `${field}-asc`) {
			onSort(`${field}-desc`);
		} else {
			onSort(`${field}-asc`);
		}
	};

	return (
		<div className='flex-1 flex flex-col w-full'>
			{patients.length === 0 ? (
				// Empty State
				<div className='flex-1 flex items-center justify-center'>
					<div className='text-center'>
						<div className='w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4'>
							<svg
								className='w-12 h-12 text-gray-400'
								fill='none'
								stroke='currentColor'
								viewBox='0 0 24 24'>
								<path
									strokeLinecap='round'
									strokeLinejoin='round'
									strokeWidth={1}
									d='M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z'
								/>
							</svg>
						</div>
						<h3 className='text-lg font-medium text-gray-900 mb-2'>
							No patients found
						</h3>
						<p className='text-gray-500 mb-4'>
							{searchTerm
								? 'Try adjusting your search terms.'
								: 'No assigned patients yet.'}
						</p>
						{!searchTerm && (
							<button
								onClick={(e) => onAddPatient(e)}
								className='inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors duration-200'>
								<PlusIcon className='w-4 h-4 mr-2' />
								Add Your First Patient
							</button>
						)}
					</div>
				</div>
			) : (
				// List View
				<div className='bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden'>
					{/* Table Header */}
					<div className='bg-gray-50 px-6 py-4 border-b border-gray-200'>
						<div className='grid grid-cols-12 gap-4 text-xs font-medium text-gray-500 uppercase tracking-wider'>
							<div
								className='col-span-3 flex items-center cursor-pointer hover:text-gray-700'
								onClick={() => handleSort('name')}>
								Patient Name
								<svg
									className={`w-3 h-3 ml-1 transform transition-transform ${
										sortBy?.startsWith('name')
											? sortBy === 'name-desc'
												? 'rotate-180'
												: '' // little transition to make the arrow flip 180 degrees
											: 'opacity-30'
									}`}
									fill='none'
									stroke='currentColor'
									viewBox='0 0 24 24'>
									<path
										strokeLinecap='round'
										strokeLinejoin='round'
										strokeWidth={2}
										d='M19 9l-7 7-7-7'
									/>
								</svg>
							</div>
							<div className='col-span-2'>NRIC</div>
							<div
								className='col-span-1 flex items-center cursor-pointer hover:text-gray-700'
								onClick={() => handleSort('age')}>
								Age
								<svg
									className={`w-3 h-3 ml-1 transform transition-transform ${
										sortBy?.startsWith('age')
											? sortBy === 'age-desc'
												? 'rotate-180'
												: ''
											: 'opacity-30'
									}`}
									fill='none'
									stroke='currentColor'
									viewBox='0 0 24 24'>
									<path
										strokeLinecap='round'
										strokeLinejoin='round'
										strokeWidth={2}
										d='M19 9l-7 7-7-7'
									/>
								</svg>
							</div>
							<div className='col-span-1'>Gender</div>
							<div
								className='col-span-3 flex items-center cursor-pointer hover:text-gray-700'
								onClick={() => handleSort('clinic')}>
								Clinic
								<svg
									className={`w-3 h-3 ml-1 transform transition-transform ${
										sortBy?.startsWith('clinic')
											? sortBy === 'clinic-desc'
												? 'rotate-180'
												: ''
											: 'opacity-30'
									}`}
									fill='none'
									stroke='currentColor'
									viewBox='0 0 24 24'>
									<path
										strokeLinecap='round'
										strokeLinejoin='round'
										strokeWidth={2}
										d='M19 9l-7 7-7-7'
									/>
								</svg>
							</div>
							<div className='col-span-2'></div>
						</div>
					</div>

					{/* Table Body */}
					<div className='divide-y divide-gray-200'>
						{patients.map((patient) => {
							// Calculate age
							const today = new Date();
							const birthDate = new Date(patient.dob);
							let age = today.getFullYear() - birthDate.getFullYear();
							const monthDiff = today.getMonth() - birthDate.getMonth();
							if (
								monthDiff < 0 ||
								(monthDiff === 0 && today.getDate() < birthDate.getDate())
							) {
								age--;
							}

							return (
								<div
									key={patient.id}
									className='px-6 py-4 hover:bg-gray-50 transition-colors duration-150'>
									<div
										onClick={() => handleViewDetails(patient.id)}
										className='grid grid-cols-12 gap-4 items-center cursor-pointer'>
										{/* Patient Name */}
										<div className='col-span-3'>
											<div className='flex items-center space-x-3'>
												{/* Avatar */}
												<div className='w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center border border-gray-300 flex-shrink-0'>
													<span className='text-sm font-semibold text-gray-600'>
														{patient.firstName.charAt(0)}
														{patient.lastName.charAt(0)}
													</span>
												</div>
												<div>
													<p className='text-sm font-semibold text-gray-900'>
														{patient.firstName} {patient.lastName}
													</p>
													<p className='text-xs text-gray-500'>
														Dr. {patient.doctor?.firstName} {patient.doctor?.lastName}
													</p>
												</div>
											</div>
										</div>

										{/* NRIC */}
										<div className='col-span-2'>
											<p className='text-sm text-gray-900 font-mono'>
												****{patient.nric.slice(-4)}
											</p>
										</div>

										{/* Age */}
										<div className='col-span-1'>
											<p className='text-sm text-gray-900'>{age}</p>
										</div>

										{/* Gender */}
										<div className='col-span-1'>
											<span
												className={`inline-flex items-center justify-center w-6 h-6 rounded-full text-xs font-bold text-white ${
													patient.gender === 'Male'
														? 'bg-blue-500'
														: 'bg-pink-500'
												}`}>
												{patient.gender === 'Male' ? 'M' : 'F'}
											</span>
										</div>

										{/* Clinic */}
										<div className='col-span-3'>
											<p className='text-sm text-gray-900 truncate'>
												{patient.clinic?.clinicName}
											</p>
											<p className='text-xs text-gray-500'>
												DOB: {patient.dob}
											</p>
										</div>

										{/* Actions */}
										<div className='col-span-2'>
											<div className='flex items-center ml-10 space-x-2'>
												<button
													onClick={(e) => {
														e.stopPropagation();
														onRemove(patient.id);
													}}
													className='p-1.5  text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-md transition-colors duration-200'
													title='Remove patient'>
													<TrashIcon className='w-4 h-4 cursor-pointer ' />
												</button>
											</div>
										</div>
									</div>
								</div>
							);
						})}
					</div>
				</div>
			)}
		</div>
	);
}