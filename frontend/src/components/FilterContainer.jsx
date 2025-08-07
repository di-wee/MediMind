import React from 'react';
import '../index.css';

function FilterContainer({
	filterOptions,
	handleFilterChange,
	selectedFilters,
}) {
	return (
		<>
			<div className='filter-grid text-center'>
				<div className='px-2'>
					<h3 className='text-xs'>Filter by:</h3>
					{filterOptions.map((op) => (
						<div className='flex items-center mt-2 mb-2'>
							<input
								id={`checkbox-${op}`}
								type='checkbox'
								className='input-checkbox'
								onChange={() => handleFilterChange(op)}
								checked={selectedFilters.includes(op)}
								value={`${op}:${
									op === 'Missed' || op === 'Inactive' ? 'false' : 'true'
								}`}
							/>
							<label
								htmlFor='default-checkbox'
								className='ms-2 text-xs text-gray-600'>
								{op}
							</label>
						</div>
					))}
				</div>
			</div>
		</>
	);
}

export default FilterContainer;
