interface ManagementListProps<T> {
  items: T[];
  columns: (keyof T)[];
  onEdit: (item: T) => void;
  onDelete: (item: T) => void;
}

const ManagementList = <T extends { id: any }>({ items, columns, onEdit, onDelete }: ManagementListProps<T>) => {
  return (
    <div className="management-list-wrapper">
      <div className="management-table-container">
        <table className="management-table">
          <thead>
            <tr>
              {columns.map((column) => (
                <th key={String(column)}>{String(column)}</th>
              ))}
              <th></th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                {columns.map((column) => (
                  <td key={String(column)}>{String(item[column])}</td>
                ))}
                <td className="table-actions">
                  <button onClick={() => onEdit(item)} className="button button-secondary">
                    Edit
                  </button>
                  <button onClick={() => onDelete(item)} className="button button-ghost text-red-600 hover:text-red-800">
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ManagementList;
