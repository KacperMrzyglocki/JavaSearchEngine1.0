import React from 'react';

const BookDetailsModal = ({ bookDetails, setShowModal }) => {
    if (!bookDetails) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
            <div className="bg-white p-6 rounded-lg shadow-lg max-w-lg w-full">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold">Book Details</h2>
                    <button
                        className="text-red-500 hover:text-red-700"
                        onClick={()=>setShowModal(false)}
                    >
                        Close
                    </button>
                </div>
                <div>
                    <pre className="bg-gray-100 p-4 rounded-lg whitespace-pre-wrap break-words">
                        {JSON.stringify(bookDetails, null, 2)}
                    </pre>
                </div>
            </div>
        </div>
    );
};

export default BookDetailsModal;
