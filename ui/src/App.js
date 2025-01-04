import React, { useState } from 'react';
import Navbar from './components/Navbar';
import SearchQuery from './components/SearchQuery';
import MetadataSearch from './components/MetadataSearch';

const App = () => {
  const [activeTab, setActiveTab] = useState('normal');

  return (
      <div className="font-sans bg-gray-100 min-h-screen">
        <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
        <div className="container mx-auto">
          {activeTab === 'normal' || activeTab === 'dictionary' || activeTab === 'folder' ? (
              <SearchQuery tab={activeTab} />
          ) : activeTab === 'metadata' ? (
              <MetadataSearch />
          ) : null}
        </div>
      </div>
  );
};

export default App;
