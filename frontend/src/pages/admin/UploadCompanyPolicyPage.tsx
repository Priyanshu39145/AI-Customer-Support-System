import { useState } from 'react';
import { Upload, FileText } from 'lucide-react';
import companyPolicyService from '@/services/companyPolicyService';
import { useToast } from '@/components/Toast/ToastProvider';

export const UploadCompanyPolicyPage = () => {
  const { showToast } = useToast();

  const [file, setFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleUpload = async () => {
    if (!file) {
      showToast('error', 'Please select a PDF file');
      return;
    }

    try {
      setIsUploading(true);

      const formData = new FormData();
      formData.append('file', file);

      const response = await companyPolicyService.uploadPolicy(formData);

      showToast('success', response || 'Company policy uploaded successfully');

      setFile(null);
    } catch (error) {
      showToast('error', 'Failed to upload company policy');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          Upload Company Policy
        </h1>

        <p className="text-gray-500">
          Upload PDF documents for AI knowledge base and RAG retrieval
        </p>
      </div>

      <div className="bg-white dark:bg-gray-800 border border-[var(--color-border)] rounded-xl p-6">
        <div className="flex items-center gap-3 mb-6">
          <FileText className="w-8 h-8 text-primary-500" />

          <div>
            <h2 className="font-semibold">
              Company Policy PDF
            </h2>

            <p className="text-sm text-gray-500">
              Only PDF files are supported
            </p>
          </div>
        </div>

        <div className="space-y-4">
          <input
            type="file"
            accept=".pdf"
            onChange={(e) => {
              const selectedFile = e.target.files?.[0] || null;
              setFile(selectedFile);
            }}
            className="input"
          />

          {file && (
            <div className="p-3 rounded-lg bg-gray-100 dark:bg-gray-700">
              <p className="text-sm font-medium">
                Selected File:
              </p>

              <p className="text-sm text-gray-500">
                {file.name}
              </p>
            </div>
          )}

          <button
            onClick={handleUpload}
            disabled={!file || isUploading}
            className="btn-primary flex items-center gap-2"
          >
            <Upload className="w-4 h-4" />

            {isUploading ? 'Uploading...' : 'Upload PDF'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default UploadCompanyPolicyPage;