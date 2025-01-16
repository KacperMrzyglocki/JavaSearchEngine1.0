import React, { useState } from 'react';
import axios from 'axios';
import BookDetailsModal from "./BookDetailsModal";
import {baseURL} from "../globals";

const SearchQuery = ({ tab }) => {
    const [queryMode, setQueryMode] = useState('normal'); // 'normal', 'and', 'or'
    const [query, setQuery] = useState('');
    const [query1, setQuery1] = useState(''); // For 'and' and 'or' modes
    const [query2, setQuery2] = useState(''); // For 'and' and 'or' modes
    const [result, setResult] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [bookDetails, setBookDetails] = useState(null)

    const handleSearch = async () => {
        if (queryMode === 'normal' && !query) return;
        if ((queryMode === 'and' || queryMode === 'or') && (!query1 || !query2)) return;

        let url = '';

        if (queryMode === 'normal') {
            url = `${baseURL}/search/${tab}/${query}`;
        } else if (queryMode === 'and') {
            url = `${baseURL}/search/${tab}/and/${encodeURIComponent(query1)}/${encodeURIComponent(query2)}`;
        } else if (queryMode === 'or') {
            url = `${baseURL}/search/${tab}/or/${encodeURIComponent(query1)}/${encodeURIComponent(query2)}`;
        }
        console.log(url)

        try {
            const response = await axios.get(url);
            setResult(response.data);
        } catch (error) {
            console.error('Error searching:', error);
            setResult(null);
        }
    };

    const handleShowModal = async (book)=>{
        try {
            const response = await axios.get(`${baseURL}/search/metadata/${book}`);
            setBookDetails(response.data);
            setShowModal(true);
        } catch (error) {
            console.error('Error searching:', error);
            setBookDetails(null);
        }
    }

    return (
        <div className="p-6">
            <div className="flex flex-col items-center">
                {/* Query Mode Selector */}
                <div className="mb-4 flex space-x-4">
                    <button
                        className={`px-4 py-2 rounded-lg ${
                            queryMode === 'normal' ? 'bg-blue-700 text-white' : 'bg-gray-200 text-black'
                        }`}
                        onClick={() => setQueryMode('normal')}
                    >
                        Normal
                    </button>
                    <button
                        className={`px-4 py-2 rounded-lg ${
                            queryMode === 'and' ? 'bg-blue-700 text-white' : 'bg-gray-200 text-black'
                        }`}
                        onClick={() => setQueryMode('and')}
                    >
                        AND
                    </button>
                    <button
                        className={`px-4 py-2 rounded-lg ${
                            queryMode === 'or' ? 'bg-blue-700 text-white' : 'bg-gray-200 text-black'
                        }`}
                        onClick={() => setQueryMode('or')}
                    >
                        OR
                    </button>
                </div>

                {/* Input Fields */}
                {queryMode === 'normal' && (
                    <input
                        type="text"
                        className="border border-gray-300 p-2 rounded-lg mb-4"
                        placeholder="Enter search query..."
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                    />
                )}
                {(queryMode === 'and' || queryMode === 'or') && (
                    <div className="flex flex-col space-y-4 mb-4">
                        <input
                            type="text"
                            className="border border-gray-300 p-2 rounded-lg"
                            placeholder="Enter first query..."
                            value={query1}
                            onChange={(e) => setQuery1(e.target.value)}
                        />
                        <input
                            type="text"
                            className="border border-gray-300 p-2 rounded-lg"
                            placeholder="Enter second query..."
                            value={query2}
                            onChange={(e) => setQuery2(e.target.value)}
                        />
                    </div>
                )}

                {/* Search Button */}
                <button
                    onClick={handleSearch}
                    className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
                >
                    Search
                </button>

                {/* Search Results */}
                {result && (
                    <div className="mt-4 w-full max-w-3xl overflow-x-auto">
                        <h3 className="text-xl font-bold">Search Results:</h3>
                        <pre className="bg-gray-100 p-4 rounded-lg whitespace-pre-wrap break-words">
                            {result.map((book)=><p key={book} className="cursor-pointer hover:text-blue-500" onClick={()=>handleShowModal(book)}>{book}</p>)}
                        </pre>
                    </div>
                )}
            </div>

            {showModal && <BookDetailsModal bookDetails={bookDetails} setShowModal={setShowModal}/>}
        </div>
    );
};

export default SearchQuery;
