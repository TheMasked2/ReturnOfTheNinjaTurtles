interface ManagementListProps<T> {
  items: T[];
  columns: (keyof T)[];
  onEdit: (item: T) => void;
  onDelete: (item: T) => void;
}

const ManagementList = <T extends { id: any }>({ items, columns, onEdit, onDelete }: ManagementListProps<T>) => {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full bg-white">
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={String(column)} className="px-6 py-3 border-b-2 border-gray-300 text-left leading-4 text-blue-500 tracking-wider">
                {String(column)}
              </th>
            ))}
            <th className="px-6 py-3 border-b-2 border-gray-300"></th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id}>
              {columns.map((column) => (
                <td key={String(column)} className="px-6 py-4 whitespace-no-wrap border-b border-gray-500">
                  {String(item[column])}
                </td>
              ))}
              <td className="px-6 py-4 whitespace-no-wrap text-right border-b border-gray-500 text-sm leading-5">
                <button onClick={() => onEdit(item)} className="text-indigo-600 hover:text-indigo-900">
                  Edit
                </button>
                <button onClick={() => onDelete(item)} className="ml-4 text-red-600 hover:text-red-900">
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ManagementList;
