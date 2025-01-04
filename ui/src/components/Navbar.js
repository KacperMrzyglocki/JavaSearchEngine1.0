import React from 'react';

const Navbar = ({ activeTab, setActiveTab }) => {
    return (
        <nav className="bg-blue-500 p-4 shadow-md">
            <div className="container mx-auto flex justify-between items-center">
                <div className="text-white text-2xl font-bold">Inverted Index UI</div>
                <div className="space-x-4">
                    <button
                        className={`px-4 py-2 rounded text-white ${
                            activeTab === 'normal' ? 'bg-blue-700' : 'hover:bg-blue-700'
                        }`}
                        onClick={() => setActiveTab('normal')}
                    >
                        Normal
                    </button>
                    <button
                        className={`px-4 py-2 rounded text-white ${
                            activeTab === 'dictionary' ? 'bg-blue-700' : 'hover:bg-blue-700'
                        }`}
                        onClick={() => setActiveTab('dictionary')}
                    >
                        Dictionary
                    </button>
                    <button
                        className={`px-4 py-2 rounded text-white ${
                            activeTab === 'folder' ? 'bg-blue-700' : 'hover:bg-blue-700'
                        }`}
                        onClick={() => setActiveTab('folder')}
                    >
                        Folder
                    </button>
                    <button
                        className={`px-4 py-2 rounded text-white ${
                            activeTab === 'metadata' ? 'bg-blue-700' : 'hover:bg-blue-700'
                        }`}
                        onClick={() => setActiveTab('metadata')}
                    >
                        Metadata
                    </button>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
