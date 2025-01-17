import React, { useState } from 'react';
import axios from 'axios';
import { baseURL } from "../globals";

const Stats = () => {
    const [selectedStat, setSelectedStat] = useState('');
    const [result, setResult] = useState(null);

    const handleFetchStat = async () => {
        if (!selectedStat) return;

        let url = '';
        switch (selectedStat) {
            case 'numberbooks':
                url = `${baseURL}/stats/numberbooks`;
                break;
            case 'booksByLanguage':
                url = `${baseURL}/stats/booksByLanguage`;
                break;
            case 'booksByAuthor':
                url = `${baseURL}/stats/booksByAuthor`;
                break;
            case 'topLanguage':
                url = `${baseURL}/stats/topLanguage`;
                break;
            case 'topAuthor':
                url = `${baseURL}/stats/topAuthor`;
                break;
            default:
                return;
        }

        try {
            const response = await axios.get(url);
            setResult(response.data);
        } catch (error) {
            console.error('Error fetching stats:', error);
            setResult(null);
        }
    };

    return (
        <div className="p-6">
            <div className="flex flex-col items-center">
                <select
                    value={selectedStat}
                    onChange={(e) => setSelectedStat(e.target.value)}
                    className="mb-4 p-2 border border-gray-300 rounded-lg"
                >
                    <option value="" disabled>Select a stat</option>
                    <option value="numberbooks">Number of Books</option>
                    <option value="booksByLanguage">Books by Language</option>
                    <option value="booksByAuthor">Books by Author</option>
                    <option value="topLanguage">Top Language</option>
                    <option value="topAuthor">Top Author</option>
                </select>

                <button
                    onClick={handleFetchStat}
                    className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
                >
                    Fetch Stat
                </button>

                {result && (
                    <div className="mt-4 w-full max-w-3xl overflow-x-auto">
                        <h3 className="text-xl font-bold">Stat Result:</h3>
                        <pre className="bg-gray-100 p-4 rounded-lg whitespace-pre-wrap break-words">
                            {JSON.stringify(result, null, 2)}
                        </pre>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Stats;
