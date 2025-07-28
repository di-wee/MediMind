import React from 'react';

function FilterContainer({ filterOptions }) {
	return (
		<>
			<div className='filter-container text-center'>
				<div className='px-2'>
					<h3 className='text-xs'>Filter by:</h3>
					{filterOptions.map((op) => (
						<div class='flex items-center mt-2 mb-2'>
							<input
								id='default-checkbox'
								type='checkbox'
								className='input-checkbox'
							/>
							<label
								for='default-checkbox'
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
