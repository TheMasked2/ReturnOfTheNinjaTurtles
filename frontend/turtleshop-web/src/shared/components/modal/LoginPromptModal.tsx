import { useNavigate } from "react-router-dom";
import { Modal } from "./Modal";

interface LoginPromptModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function LoginPromptModal({ isOpen, onClose }: LoginPromptModalProps) {
  const navigate = useNavigate();

  return (
    <Modal isOpen={isOpen} title="Login required" onClose={onClose}>
      <p>You must be logged in to add items to your cart or wishlist.</p>
      <div className="modal-actions">
        <button className="button button-ghost" type="button" onClick={onClose}>
          Cancel
        </button>
        <button
          className="button button-primary"
          type="button"
          onClick={() => {
            onClose();
            navigate("/login");
          }}
        >
          Go to login
        </button>
      </div>
    </Modal>
  );
}