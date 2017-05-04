import React from "react";
import Modal from "react-bootstrap/lib/Modal";
import {Button} from "semantic-ui-react";

class BookPreviewPopup extends React.Component {

    constructor(props) {
        super(props);

        this.state = {showModal: false};
    }

    close = () => {
        this.setState({showModal: false});
    }

    open = (book) => {
        this.setState({showModal: true, book: book});
    }

    render() {
        let title = this.state.book != undefined ? this.state.book.title : "";

        if (this.state.showModal)
            return (
                <Modal show={this.state.showModal} onHide={this.close}>
                    <Modal.Header closeButton>
                        <Modal.Title>Book preview</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {title}
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.close}>Close</Button>
                    </Modal.Footer>
                </Modal>)
        else
            return (<div/>);
    }
}

export default BookPreviewPopup;