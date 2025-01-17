import React, { useState } from 'react';
import axios from 'axios';
import {baseURL} from "../globals";

const MetadataSearch = () => {
    const [searchType, setSearchType] = useState('id');
    const [query, setQuery] = useState('');
    const [result, setResult] = useState(null);

    const handleSearch = async () => {
        if (!query) return;

        let url = '';

        if (searchType === 'id') {
            url = `${baseURL}/search/metadata/${query}`;
        } else if (searchType === 'author') {
            url = `${baseURL}/search/metadata/author/${query}`;
        } else if (searchType === 'language') {
            url = `${baseURL}/search/metadata/language/${query}`;
        } else if (searchType === 'title') {
            url = `${baseURL}/search/metadata/title/${query}`;
        }

        try {
            const response = await axios.get(url);
            setResult(response.data);
        } catch (error) {
            console.error('Error searching:', error);
            setResult(null);
        }
    };

    return (
        <div className="p-6">
            <div className="flex flex-col items-center">
                <select
                    value={searchType}
                    onChange={(e) => setSearchType(e.target.value)}
                    className="mb-4 p-2 border border-gray-300 rounded-lg"
                >
                    <option value="id">Search by ID</option>
                    <option value="author">Search by Author</option>
                    <option value="language">Search by Language</option>
                    <option value="title">Search by Title</option>
                </select>

                {/* Dynamiczne pole wejściowe */}
                {searchType === 'language' ? (
                    <select
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        className="border border-gray-300 p-2 rounded-lg mb-4"
                    >
                        <option value="" disabled>Select a language</option>
                        <option value="en">English (en)</option>
                        <option value="es">Spanish (es)</option>
                        <option value="pl">Polish (pl)</option>
                        <option value="it">Italian (it)</option>
                        <option value="de">German (de)</option>
                    </select>
                ) : (
                    <input
                        type="text"
                        className="border border-gray-300 p-2 rounded-lg mb-4"
                        placeholder={`Enter ${searchType}...`}
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                    />
                )}

                <button
                    onClick={handleSearch}
                    className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
                >
                    Search
                </button>

                {result && (
                    <div className="mt-4 w-full max-w-3xl overflow-x-auto">
                        <h3 className="text-xl font-bold">Search Results:</h3>
                        <pre className="bg-gray-100 p-4 rounded-lg whitespace-pre-wrap break-words">
                            {JSON.stringify(result, null, 2)}
                        </pre>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MetadataSearch;
